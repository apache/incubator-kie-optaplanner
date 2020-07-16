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

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriGroupByAccumulator;

class TriGroupBy2Map1CollectMutator<A, B, C, NewA, NewB, NewC> extends AbstractTriGroupByMutator<A, B, C> {

    private final TriFunction<A, B, C, NewA> groupKeyMappingA;
    private final TriFunction<A, B, C, NewB> groupKeyMappingB;
    private final TriConstraintCollector<A, B, C, ?, NewC> collectorC;

    public TriGroupBy2Map1CollectMutator(TriFunction<A, B, C, NewA> groupKeyMappingA,
            TriFunction<A, B, C, NewB> groupKeyMappingB, TriConstraintCollector<A, B, C, ?, NewC> collectorC) {
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.collectorC = collectorC;
    }

    @Override
    public AbstractRuleBuilder apply(AbstractRuleBuilder ruleBuilder) {
        return groupBiWithCollect(ruleBuilder, () -> new DroolsTriGroupByAccumulator<>(groupKeyMappingA,
                groupKeyMappingB, collectorC, ruleBuilder.getVariables().get(0), ruleBuilder.getVariables().get(1),
                ruleBuilder.getVariables().get(2)));
    }
}
