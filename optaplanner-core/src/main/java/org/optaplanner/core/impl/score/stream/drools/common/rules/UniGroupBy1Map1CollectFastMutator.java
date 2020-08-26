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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ExprViewItem;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniAccumulateFunction;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.drools.model.DSL.accFunction;
import static org.drools.model.PatternDSL.pattern;

final class UniGroupBy1Map1CollectFastMutator<A, NewA, NewB> extends AbstractUniGroupByMutator {

    private final Function<A, NewA> groupKeyMappingA;
    private final UniConstraintCollector<A, ?, NewB> collectorB;

    public UniGroupBy1Map1CollectFastMutator(Function<A, NewA> groupKeyMappingA,
            UniConstraintCollector<A, ?, NewB> collectorB) {
        this.groupKeyMappingA = groupKeyMappingA;
        this.collectorB = collectorB;
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        ruleAssembler.applyFilterToLastPrimaryPattern();
        Variable<A> input = ruleAssembler.getVariable(0);
        Variable<NewA> groupKey = (Variable<NewA>) PatternDSL.declarationOf(Object.class);
        Variable<NewB> output = (Variable<NewB>) PatternDSL.declarationOf(Object.class);
        DroolsUniAccumulateFunction<A, ?, NewB> bridge = new DroolsUniAccumulateFunction<>(collectorB);
        ExprViewItem<NewB> accumulatePattern = PatternDSL.groupBy(getInnerAccumulatePattern(ruleAssembler), input,
                groupKey, groupKeyMappingA::apply, accFunction(() -> bridge, input).as(output));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleAssembler.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern); // The last pattern is added here.
        PatternDSL.PatternDef<NewB> newPrimaryPattern = pattern(output);
        return new BiRuleAssembler(ruleAssembler::generateNextId, ruleAssembler.getExpectedGroupByCount(),
                newFinishedExpressions, Arrays.asList(groupKey, output), singletonList(newPrimaryPattern), emptyMap());
    }
}
