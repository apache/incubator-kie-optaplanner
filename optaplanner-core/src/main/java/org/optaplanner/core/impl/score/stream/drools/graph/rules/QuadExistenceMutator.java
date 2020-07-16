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
import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.FilteringPentaJoiner;
import org.optaplanner.core.impl.score.stream.penta.NonePentaJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

final class QuadExistenceMutator<A, B, C, D, E> implements Mutator {

    private final boolean shouldExist;
    private final Class<E> otherFactType;
    private final AbstractPentaJoiner<A, B, C, D, E>[] joiners;

    public QuadExistenceMutator(AbstractConstraintModelJoiningNode<E, AbstractPentaJoiner<A, B, C, D, E>> node,
            boolean shouldExist) {
        this.shouldExist = shouldExist;
        this.otherFactType = node.getOtherFactType();
        this.joiners = node.get().stream()
                .toArray(AbstractPentaJoiner[]::new);
    }

    private AbstractRuleBuilder applyJoiners(AbstractRuleBuilder ruleBuilder, AbstractPentaJoiner<A, B, C, D, E> joiner,
            PentaPredicate<A, B, C, D, E> predicate) {
        if (joiner == null) {
            return applyFilters(ruleBuilder, predicate);
        }
        // There is no epsilon index in Drools, therefore we replace joining with a filter.
        PentaPredicate<A, B, C, D, E> joinFilter = joiner::matches;
        PentaPredicate<A, B, C, D, E> result = predicate == null ? joinFilter : joinFilter.and(predicate);
        // And finally we add the filter to the E pattern,
        return applyFilters(ruleBuilder, result);
    }

    private AbstractRuleBuilder applyFilters(AbstractRuleBuilder ruleBuilder, PentaPredicate<A, B, C, D, E> predicate) {
        Variable<E> toExist = PatternDSL.declarationOf(otherFactType, ruleBuilder.generateNextId("quadToExist"));
        PatternDSL.PatternDef<E> existencePattern = PatternDSL.pattern(toExist);
        Variable[] variables = ruleBuilder.getVariables().toArray(new Variable[0]);
        PatternDSL.PatternDef<E> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, variables[0], variables[1], variables[2],
                        variables[3], (e, a, b, c, d) -> predicate.test((A) a, (B) b, (C) c, (D) d, e));
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
        AbstractPentaJoiner<A, B, C, D, E> finalJoiner = null;
        PentaPredicate<A, B, C, D, E> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractPentaJoiner<A, B, C, D, E> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NonePentaJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringPentaJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractPentaJoiner.merge(finalJoiner, joiner);
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
