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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import java.util.List;
import java.util.Map;

import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;

final class QuadJoinMutator<A, B, C, D> implements JoinMutator {

    private final AbstractQuadJoiner<A, B, C, D> joiner;

    public QuadJoinMutator(AbstractConstraintModelJoiningNode<D, AbstractQuadJoiner<A, B, C, D>> node) {
        this.joiner = node.get().get(0);
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler leftRuleAssembler, AbstractRuleAssembler rightRuleAssembler) {
        rightRuleAssembler.getPrimaryPatterns().get(rightRuleAssembler.getPrimaryPatterns().size() - 1)
                .expr("Filter using " + joiner, leftRuleAssembler.getVariables().get(0),
                        leftRuleAssembler.getVariables().get(1), leftRuleAssembler.getVariables().get(2),
                        rightRuleAssembler.getVariables().get(0),
                        (fact, a, b, c, d) -> joiner.matches((A) a, (B) b, (C) c, (D) d));
        return merge(leftRuleAssembler, rightRuleAssembler);
    }

    @Override
    public AbstractRuleAssembler newRuleAssembler(AbstractRuleAssembler leftRuleAssembler,
            AbstractRuleAssembler rightRuleAssembler,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        return new QuadRuleAssembler(leftRuleAssembler::generateNextId,
                Math.max(leftRuleAssembler.getExpectedGroupByCount(), rightRuleAssembler.getExpectedGroupByCount()),
                finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

}
