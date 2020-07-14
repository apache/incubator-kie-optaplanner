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

import static org.drools.model.PatternDSL.PatternDef;
import static org.drools.model.PatternDSL.betaIndexedBy;
import static org.drools.model.PatternDSL.declarationOf;

import java.util.function.Function;

import org.drools.model.BetaIndex;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate2;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;

final class BiJoinMutator<A, B> implements JoinMutator {

    private final Class<B> otherFactType;
    private final AbstractBiJoiner<A, B> biJoiner;

    public BiJoinMutator(AbstractConstraintModelJoiningNode<B, AbstractBiJoiner<A, B>> node) {
        this.otherFactType = node.getOtherFactType();
        this.biJoiner = node.get().get(0);
    }

    @Override
    public AbstractRuleBuilder apply(AbstractRuleBuilder leftRuleBuilder, AbstractRuleBuilder rightRuleBuilder) {
        JoinerType[] joinerTypes = biJoiner.getJoinerTypes();
        // We rebuild the A pattern, binding variables for left parts of the joins.
        PatternDef aJoiner = leftRuleBuilder.getPrimaryPatterns()
                .get(leftRuleBuilder.getPrimaryPatterns().size() - 1);
        Variable[] joinVars = new Variable[joinerTypes.length];
        for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
            // For each mapping, bind one join variable.
            int currentMappingIndex = mappingIndex;
            Variable<Object> joinVar = declarationOf(Object.class, "joinVar" + currentMappingIndex);
            Function<A, Object> leftMapping = biJoiner.getLeftMapping(currentMappingIndex);
            aJoiner.bind(joinVar, a -> leftMapping.apply((A) a));
            joinVars[currentMappingIndex] = joinVar;
        }
        PatternDef bJoiner = rightRuleBuilder.getPrimaryPatterns()
                .get(rightRuleBuilder.getPrimaryPatterns().size() - 1);
        for (int mappingIndex = 0; mappingIndex < joinerTypes.length; mappingIndex++) {
            // For each mapping, bind a join variable from A to B and index the binding.
            int currentMappingIndex = mappingIndex;
            JoinerType joinerType = joinerTypes[currentMappingIndex];
            Function<A, Object> leftMapping = biJoiner.getLeftMapping(currentMappingIndex);
            Function<B, Object> rightMapping = biJoiner.getRightMapping(currentMappingIndex);
            Function1<B, Object> rightExtractor = rightMapping::apply;
            Predicate2<B, A> predicate = (b, a) -> { // We only extract B; A is coming from a pre-bound join var.
                return joinerType.matches(a, rightExtractor.apply(b));
            };
            BetaIndex<B, A, Object> index = betaIndexedBy(Object.class, Mutator.getConstraintType(joinerType),
                    currentMappingIndex, rightExtractor, leftMapping::apply);
            bJoiner.expr("Join using joiner #" + currentMappingIndex + " in " + biJoiner,
                    joinVars[currentMappingIndex], predicate, index);
        }
        return merge(leftRuleBuilder, rightRuleBuilder);
    }

    @Override
    public AbstractRuleBuilder newRuleBuilder(AbstractRuleBuilder leftRuleBuilder, AbstractRuleBuilder rightRuleBuilder) {
        return new BiRuleBuilder(leftRuleBuilder::generateNextId,
                Math.max(leftRuleBuilder.getExpectedGroupByCount(), rightRuleBuilder.getExpectedGroupByCount()));
    }
}
