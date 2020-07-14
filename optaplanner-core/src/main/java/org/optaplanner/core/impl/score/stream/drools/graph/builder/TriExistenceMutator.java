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

package org.optaplanner.core.impl.score.stream.drools.graph.builder;

import java.util.ArrayList;
import java.util.Arrays;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ExprViewItem;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.FilteringQuadJoiner;
import org.optaplanner.core.impl.score.stream.quad.NoneQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.NoneTriJoiner;

public class TriExistenceMutator<A, B, C, D> implements Mutator {

    private final boolean shouldExist;
    private final Class<D> otherFactType;
    private final AbstractQuadJoiner<A, B, C, D>[] joiners;

    public TriExistenceMutator(AbstractConstraintModelJoiningNode<D, AbstractQuadJoiner<A, B, C, D>> node,
            boolean shouldExist) {
        this.shouldExist = shouldExist;
        this.otherFactType = node.getOtherFactType();
        this.joiners = node.get().stream()
                .toArray(AbstractQuadJoiner[]::new);
    }

    private RuleBuilder applyJoiners(RuleBuilder ruleBuilder, AbstractQuadJoiner<A, B, C, D> joiner,
            QuadPredicate<A, B, C, D> predicate) {
        PatternDSL.PatternDef aPattern = ruleBuilder.getPrimaryPatterns().get(0);
        PatternDSL.PatternDef bPattern = ruleBuilder.getPrimaryPatterns().get(1);
        PatternDSL.PatternDef cPattern = ruleBuilder.getPrimaryPatterns().get(2);
        if (joiner == null) {
            return applyFilters(ruleBuilder, aPattern, bPattern, cPattern, predicate);
        }
        // There is no delta index in Drools, therefore we replace joining with a filter.
        QuadPredicate<A, B, C, D> joinFilter = joiner::matches;
        QuadPredicate<A, B, C, D> result = predicate == null ? joinFilter : joinFilter.and(predicate);
        // And finally we add the filter to the C pattern
        return applyFilters(ruleBuilder, aPattern, bPattern, cPattern, result);
    }

    private RuleBuilder applyFilters(RuleBuilder ruleBuilder, PatternDSL.PatternDef<A> primaryPatternA,
            PatternDSL.PatternDef<B> primaryPatternB, PatternDSL.PatternDef<C> primaryPatternC,
            QuadPredicate<A, B, C, D> predicate) {
        Variable<D> toExist = PatternDSL.declarationOf(otherFactType, ruleBuilder.generateNextId("triToExist"));
        PatternDSL.PatternDef<D> existencePattern = PatternDSL.pattern(toExist);
        PatternDSL.PatternDef<D> possiblyFilteredExistencePattern = predicate == null ? existencePattern
                : existencePattern.expr("Filter using " + predicate, primaryPatternA.getFirstVariable(),
                        primaryPatternB.getFirstVariable(), primaryPatternC.getFirstVariable(),
                        (d, a, b, c) -> predicate.test(a, b, c, d));
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
    public RuleBuilder apply(RuleBuilder ruleBuilder) {
        int indexOfFirstFilter = -1;
        // Prepare the joiner and filter that will be used in the pattern
        AbstractQuadJoiner<A, B, C, D> finalJoiner = null;
        QuadPredicate<A, B, C, D> finalFilter = null;
        for (int i = 0; i < joiners.length; i++) {
            AbstractQuadJoiner<A, B, C, D> joiner = joiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (joiner instanceof NoneQuadJoiner && joiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneTriJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(joiners) + " instead.");
            } else if (!(joiner instanceof FilteringQuadJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + joiner + ") must not follow a filtering joiner ("
                            + joiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? joiner : AbstractQuadJoiner.merge(finalJoiner, joiner);
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
