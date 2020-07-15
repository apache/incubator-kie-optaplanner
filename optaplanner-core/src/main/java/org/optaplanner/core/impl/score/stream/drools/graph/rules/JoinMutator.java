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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;

interface JoinMutator extends BinaryOperator<AbstractRuleBuilder> {

    default AbstractRuleBuilder merge(AbstractRuleBuilder leftRuleBuilder, AbstractRuleBuilder rightRuleBuilder) {
        leftRuleBuilder.applyFilterToLastPrimaryPattern(leftRuleBuilder.getVariables().toArray(new Variable[0]));
        rightRuleBuilder.applyFilterToLastPrimaryPattern(rightRuleBuilder.getVariables().toArray(new Variable[0]));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(leftRuleBuilder.getFinishedExpressions());
        newFinishedExpressions.addAll(rightRuleBuilder.getFinishedExpressions());
        List<Variable> newVariables = new ArrayList<>(leftRuleBuilder.getVariables());
        newVariables.addAll(rightRuleBuilder.getVariables());
        List<PatternDef> newPrimaryPatterns = new ArrayList<>(leftRuleBuilder.getPrimaryPatterns());
        Map<Integer, List<ViewItem>> newDependentExpressionMap = new HashMap<>(leftRuleBuilder.getDependentExpressionMap());
        int startingPatternId = newPrimaryPatterns.size();
        for (int i = 0; i < rightRuleBuilder.getPrimaryPatterns().size(); i++) {
            newPrimaryPatterns.add(rightRuleBuilder.getPrimaryPatterns().get(i));
            int newPatternId = startingPatternId + i;
            newDependentExpressionMap
                    .put(newPatternId, rightRuleBuilder.getDependentExpressionMap().getOrDefault(i, new ArrayList<>(0)));
        }
        return newRuleBuilder(leftRuleBuilder, rightRuleBuilder, newFinishedExpressions, newVariables,
                newPrimaryPatterns, newDependentExpressionMap);
    }

    AbstractRuleBuilder newRuleBuilder(AbstractRuleBuilder leftRuleBuilder, AbstractRuleBuilder rightRuleBuilder,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap);

}
