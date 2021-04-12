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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.math.BigDecimal;
import java.util.Objects;

import org.drools.model.DSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;

final class TriRuleContext<A, B, C> extends AbstractRuleContext {

    private final Variable<A> variableA;
    private final Variable<B> variableB;
    private final Variable<C> variableC;

    public TriRuleContext(Variable<A> variableA, Variable<B> variableB, Variable<C> variableC,
            ViewItem<?>... viewItems) {
        super(viewItems);
        this.variableA = Objects.requireNonNull(variableA);
        this.variableB = Objects.requireNonNull(variableB);
        this.variableC = Objects.requireNonNull(variableC);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(ToIntTriFunction<A, B, C> matchWeighter) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreHolderGlobal) -> DSL.on(scoreHolderGlobal, variableA, variableB, variableC)
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, drools, scoreHolder,
                                matchWeighter.applyAsInt(a, b, c), a, b, c));
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(ToLongTriFunction<A, B, C> matchWeighter) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreHolderGlobal) -> DSL.on(scoreHolderGlobal, variableA, variableB, variableC)
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, drools, scoreHolder,
                                matchWeighter.applyAsLong(a, b, c), a, b, c));
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder(TriFunction<A, B, C, BigDecimal> matchWeighter) {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreHolderGlobal) -> DSL.on(scoreHolderGlobal, variableA, variableB, variableC)
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, drools, scoreHolder,
                                matchWeighter.apply(a, b, c), a, b, c));
        return assemble(consequenceBuilder);
    }

    public <Solution_> RuleBuilder<Solution_> newRuleBuilder() {
        ConsequenceBuilder<Solution_> consequenceBuilder =
                (constraint, scoreHolderGlobal) -> DSL.on(scoreHolderGlobal, variableA, variableB, variableC)
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(drools, scoreHolder, a, b, c));
        return assemble(consequenceBuilder);
    }

}
