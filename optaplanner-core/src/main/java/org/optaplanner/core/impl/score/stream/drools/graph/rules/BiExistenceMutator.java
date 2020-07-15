/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.drools.graph.rules;

import java.util.ArrayList;
import java.util.Arrays;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ExprViewItem;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.FilteringTriJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

public class BiExistenceMutator<A, B, C> implements Mutator {

    private final boolean shouldExist;
    private final Class<C> otherFactType;
    private final AbstractTriJoiner<A, B, C>[] joiners;

    public BiExistenceMutator(AbstractConstraintModelJoiningNode<C, AbstractTriJoiner<A, B, C>> node,
            boolean shouldExist) {
        this.shouldExist = shouldExist;
        this.otherFactType = node.getOtherFactType();
        this.joiners = node.get().stream()
                .toArray(AbstractTriJoiner[]::new);
    }

    private AbstractRuleBuilder applyJoiners(AbstractRuleBuilder ruleBuilder, AbstractTriJoiner<A, B, C> joiner,
            TriPredicate<A, B, C> predicate) {
        if (joiner == null) {
            return applyFilters(ruleBuilder, predicate);
        }
        // There is no gamma index in Drools, therefore we replace joining with a filter.
        TriPredicate<A, B, C> joinFilter = joiner::matches;
        TriPredicate<A, B, C> result = predicate == null ? joinFilter : joinFilter.and(predicate);
        // And finally we add the filter to the C pattern.
        return applyFilters(ruleBuilder, result);
    }

    private AbstractRuleBuilder applyFilters(AbstractRuleBuilder ruleBuilder, TriPredicate<A, B, C> predicate) {
        Variable<C> toExist = PatternDSL.declarationOf(otherFactType, ruleBuilder.generateNextId("biToExist"));
        PatternDSL.PatternDef<C> existencePattern = PatternDSL.pattern(toExist);
        Variable[] variables = ruleBuilder.getVariables().toArray(new Variable[0]);
        PatternDSL.PatternDef<C> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, variables[0], variables[1],
                        (c, a, b) -> predicate.test((A) a, (B) b, c));
        ExprViewItem existenceExpression = PatternDSL.exists(possiblyFilteredExistencePattern);
        if (!shouldExist) {
            existenceExpression = PatternDSL.not(possiblyFilteredExistencePattern);
        }
        int lastPatternId = ruleBuilder.getPrimaryPatterns().size() - 1;
        ruleBuilder.getDependentExpressionMap()
                .computeIfAbsent(lastPatternId, key -> new ArrayList<>(1))
                .add(existenceExpression);
        return ruleBuilder;
    }

    @Override
    public AbstractRuleBuilder apply(AbstractRuleBuilder ruleBuilder) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        AbstractTriJoiner<A, B, C> finalJoiner = null;
        TriPredicate<A, B, C> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractTriJoiner<A, B, C> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NoneTriJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringTriJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractTriJoiner.merge(finalJoiner, joiner);
                }
            } else {
                if (!hasAFilter) { // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // We merge all filters into one, so that we don't pay the penalty for lack of indexing more than once.
                finalFilter = finalFilter == null ? joiner.getFilter() : finalFilter.and(joiner.getFilter());
            }
        }
        return applyJoiners(ruleBuilder, finalJoiner, finalFilter);
    }

}
