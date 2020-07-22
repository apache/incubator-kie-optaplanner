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

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriToQuadGroupByAccumulator;

final class TriGroupBy2Map2CollectMutator<A, B, C, NewA, NewB, NewC, NewD> extends AbstractTriGroupByMutator {

    private final TriFunction<A, B, C, NewA> groupKeyMappingA;
    private final TriFunction<A, B, C, NewB> groupKeyMappingB;
    private final TriConstraintCollector<A, B, C, ?, NewC> collectorC;
    private final TriConstraintCollector<A, B, C, ?, NewD> collectorD;

    public TriGroupBy2Map2CollectMutator(TriFunction<A, B, C, NewA> groupKeyMappingA,
            TriFunction<A, B, C, NewB> groupKeyMappingB, TriConstraintCollector<A, B, C, ?, NewC> collectorC,
            TriConstraintCollector<A, B, C, ?, NewD> collectorD) {
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.collectorC = collectorC;
        this.collectorD = collectorD;
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        return groupBiWithCollectBi(ruleAssembler, () -> new DroolsTriToQuadGroupByAccumulator<>(groupKeyMappingA,
                groupKeyMappingB, collectorC, collectorD, ruleAssembler.getVariable(0), ruleAssembler.getVariable(1),
                ruleAssembler.getVariable(2)));
    }
}
