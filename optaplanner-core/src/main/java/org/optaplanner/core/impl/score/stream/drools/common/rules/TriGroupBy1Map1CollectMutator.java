/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriAccumulateFunction;

final class TriGroupBy1Map1CollectMutator<A, B, C, NewA, NewB> extends AbstractTriGroupByMutator {

    private final TriFunction<A, B, C, NewA> groupKeyMapping;
    private final TriConstraintCollector<A, B, C, ?, NewB> collectorB;

    public TriGroupBy1Map1CollectMutator(TriFunction<A, B, C, NewA> groupKeyMapping,
            TriConstraintCollector<A, B, C, ?, NewB> collectorB) {
        this.groupKeyMapping = groupKeyMapping;
        this.collectorB = collectorB;
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        TriRuleAssembler triRuleAssembler = ((TriRuleAssembler) ruleAssembler);
        return new BiRuleAssembler(
                triRuleAssembler.leftHandSide.groupBy(groupKeyMapping, new DroolsTriAccumulateFunction<>(collectorB)));
    }
}
