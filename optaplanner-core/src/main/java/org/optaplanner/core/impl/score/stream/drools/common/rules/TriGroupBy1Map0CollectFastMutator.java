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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.drools.model.PatternDSL.from;
import static org.drools.model.PatternDSL.groupBy;
import static org.drools.model.PatternDSL.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;

final class TriGroupBy1Map0CollectFastMutator<A, B, C, NewA> extends AbstractTriGroupByMutator {

    private final TriFunction<A, B, C, NewA> groupKeyMappingA;

    public TriGroupBy1Map0CollectFastMutator(TriFunction<A, B, C, NewA> groupKeyMapping) {
        this.groupKeyMappingA = groupKeyMapping;
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        ruleAssembler.applyFilterToLastPrimaryPattern();
        Variable<A> inputA = ruleAssembler.getVariable(0);
        Variable<B> inputB = ruleAssembler.getVariable(1);
        Variable<C> inputC = ruleAssembler.getVariable(2);
        Variable<NewA> groupKey = ruleAssembler.createVariable(BiTuple.class, "groupKey");
        ViewItem groupByPattern = groupBy(getInnerAccumulatePattern(ruleAssembler), inputA, inputB, inputC, groupKey,
                groupKeyMappingA::apply);
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleAssembler.getFinishedExpressions());
        newFinishedExpressions.add(groupByPattern); // The last pattern is added here.
        Variable<NewA> newA = ruleAssembler.createVariable("newA", from(groupKey));
        PatternDSL.PatternDef<NewA> newPrimaryPattern = pattern(newA);
        return new UniRuleAssembler(ruleAssembler, ruleAssembler.getExpectedGroupByCount(), newFinishedExpressions,
                Arrays.asList(newA), singletonList(newPrimaryPattern), emptyMap());
    }
}
