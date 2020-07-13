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

import static org.drools.model.PatternDSL.betaIndexedBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.drools.model.BetaIndex;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.functions.Predicate2;
import org.drools.model.view.ExprViewItem;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.FilteringBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.NoneBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;

public class UniExistenceMutator<A, B> implements Mutator {

    private final boolean shouldExist;
    private final Class<B> otherFactType;
    private final AbstractBiJoiner<A, B>[] biJoiners;

    public UniExistenceMutator(AbstractConstraintModelJoiningNode<B, AbstractBiJoiner<A, B>> node,
            boolean shouldExist) {
        this.shouldExist = shouldExist;
        this.otherFactType = node.getOtherFactType();
        this.biJoiners = node.get().stream()
                .toArray(AbstractBiJoiner[]::new);
    }

    public static Index.ConstraintType getConstraintType(JoinerType type) {
        switch (type) {
            case EQUAL:
                return Index.ConstraintType.EQUAL;
            case LESS_THAN:
                return Index.ConstraintType.LESS_THAN;
            case LESS_THAN_OR_EQUAL:
                return Index.ConstraintType.LESS_OR_EQUAL;
            case GREATER_THAN:
                return Index.ConstraintType.GREATER_THAN;
            case GREATER_THAN_OR_EQUAL:
                return Index.ConstraintType.GREATER_OR_EQUAL;
            default:
                throw new IllegalStateException("Unsupported joiner type (" + type + ").");
        }
    }

    private RuleBuilder applyJoiners(RuleBuilder ruleBuilder, AbstractBiJoiner<A, B> biJoiner,
            BiPredicate<A, B> biPredicate) {
        PatternDSL.PatternDef<A> primaryPattern =
                ruleBuilder.getPrimaryPatterns().get(ruleBuilder.getPrimaryPatterns().size() - 1);
        Variable<B> toExist = PatternDSL.declarationOf(otherFactType, "toExist");
        PatternDSL.PatternDef<B> existencePattern = PatternDSL.pattern(toExist);
        if (biJoiner == null) {
            return applyFilters(ruleBuilder, primaryPattern, existencePattern, biPredicate);
        }
        JoinerType[] joinerTypes = biJoiner.getJoinerTypes();
        // We rebuild the A pattern, binding variables for left parts of the joins.
        Variable[] joinVars = new Variable[joinerTypes.length];
        for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
            // For each mapping, bind one join variable.
            int currentMappingIndex = mappingIndex;
            Variable<Object> joinVar = PatternDSL.declarationOf(Object.class, "joinVar" + currentMappingIndex);
            Function<A, Object> leftMapping = biJoiner.getLeftMapping(currentMappingIndex);
            primaryPattern = primaryPattern.bind(joinVar, leftMapping::apply);
            joinVars[currentMappingIndex] = joinVar;
        }
        // We create the B pattern, joining with the new A pattern using its freshly bound join variables.
        for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
            // For each mapping, bind a join variable from A to B and index the binding.
            int currentMappingIndex = mappingIndex;
            JoinerType joinerType = joinerTypes[currentMappingIndex];
            Function<A, Object> leftMapping = biJoiner.getLeftMapping(currentMappingIndex);
            Function<B, Object> rightMapping = biJoiner.getRightMapping(currentMappingIndex);
            Predicate2<B, A> predicate = (b, a) -> { // We only extract B; A is coming from a pre-bound join var.
                return joinerType.matches(a, rightMapping.apply(b));
            };
            BetaIndex<B, A, ?> index = betaIndexedBy(Object.class, getConstraintType(joinerType),
                    currentMappingIndex, rightMapping::apply, leftMapping::apply);
            existencePattern = existencePattern.expr("Join using joiner #" + currentMappingIndex + " in " + biJoiner,
                    joinVars[currentMappingIndex], predicate, index);
        }
        // And finally we add the filter to the B pattern
        return applyFilters(ruleBuilder, primaryPattern, existencePattern, biPredicate);
    }

    private RuleBuilder applyFilters(RuleBuilder ruleBuilder, PatternDSL.PatternDef<A> primaryPattern,
            PatternDSL.PatternDef<B> existencePattern, BiPredicate<A, B> biPredicate) {
        PatternDSL.PatternDef<B> possiblyFilteredExistencePattern = biPredicate == null ? existencePattern
                : existencePattern.expr("Filter using " + biPredicate, primaryPattern.getFirstVariable(),
                        (b, a) -> biPredicate.test(a, b));
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
        // Prepare the joiner and filter that will be used in the pattern.
        AbstractBiJoiner<A, B> finalJoiner = null;
        BiPredicate<A, B> finalFilter = null;
        for (int i = 0; i < biJoiners.length; i++) {
            AbstractBiJoiner<A, B> biJoiner = biJoiners[i];
            boolean hasAFilter = indexOfFirstFilter >= 0;
            if (biJoiner instanceof NoneBiJoiner && biJoiners.length > 1) {
                throw new IllegalStateException("If present, " + NoneBiJoiner.class + " must be the only joiner, got "
                        + Arrays.toString(biJoiners) + " instead.");
            } else if (!(biJoiner instanceof FilteringBiJoiner)) {
                if (hasAFilter) {
                    throw new IllegalStateException("Indexing joiner (" + biJoiner + ") must not follow a filtering joiner ("
                            + biJoiners[indexOfFirstFilter] + ").");
                } else { // Merge this Joiner with the existing Joiners.
                    finalJoiner = finalJoiner == null ? biJoiner : AbstractBiJoiner.merge(finalJoiner, biJoiner);
                }
            } else {
                if (!hasAFilter) { // From now on, we only allow filtering joiners.
                    indexOfFirstFilter = i;
                }
                // We merge all filters into one, so that we don't pay the penalty for lack of indexing more than once.
                finalFilter = finalFilter == null ? biJoiner.getFilter() : finalFilter.and(biJoiner.getFilter());
            }
        }
        return applyJoiners(ruleBuilder, finalJoiner, finalFilter);
    }

}
