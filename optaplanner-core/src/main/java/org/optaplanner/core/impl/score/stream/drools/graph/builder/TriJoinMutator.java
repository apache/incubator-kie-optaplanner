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

import org.drools.model.PatternDSL;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

public class TriJoinMutator<A, B, C> implements JoinMutator {

    private final AbstractTriJoiner<A, B, C> joiner;

    public TriJoinMutator(AbstractConstraintModelJoiningNode<C, AbstractTriJoiner<A, B, C>> node) {
        this.joiner = node.get().get(0);
    }

    @Override
    public RuleBuilder apply(RuleBuilder leftRuleBuilder, RuleBuilder rightRuleBuilder) {
        PatternDSL.PatternDef<A> aPattern = leftRuleBuilder.getPrimaryPatterns().get(0);
        PatternDSL.PatternDef<B> bPattern = leftRuleBuilder.getPrimaryPatterns().get(1);
        PatternDSL.PatternDef<C> cPattern =
                rightRuleBuilder.getPrimaryPatterns().get(rightRuleBuilder.getPrimaryPatterns().size() - 1);
        cPattern = cPattern.expr("Filter using " + joiner, aPattern.getFirstVariable(),
                bPattern.getFirstVariable(), (c, a, b) -> joiner.matches(a, b, c));
        // And finally we merge the left and right side into one.
        leftRuleBuilder.applyFilterToLastPrimaryPattern();
        int startingPatternId = leftRuleBuilder.getPrimaryPatterns().size();
        for (int i = 0; i < rightRuleBuilder.getPrimaryPatterns().size(); i++) {
            leftRuleBuilder.getPrimaryPatterns().add(rightRuleBuilder.getPrimaryPatterns().get(i));
            int newPatternId = startingPatternId + i;
            leftRuleBuilder.getDependentExpressionMap()
                    .put(newPatternId, rightRuleBuilder.getDependentExpressionMap().getOrDefault(i, new ArrayList<>(0)));
            leftRuleBuilder.setFilterToApplyToLastPrimaryPattern(rightRuleBuilder.getFilterToApplyToLastPrimaryPattern());
        }
        leftRuleBuilder.getVariables().addAll(rightRuleBuilder.getVariables());
        return leftRuleBuilder;
    }

}
