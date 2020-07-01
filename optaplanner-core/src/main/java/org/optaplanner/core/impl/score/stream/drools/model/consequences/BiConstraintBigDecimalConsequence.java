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

package org.optaplanner.core.impl.score.stream.drools.model.consequences;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import org.optaplanner.core.impl.score.stream.drools.model.BiConstraintModelNode;

final class BiConstraintBigDecimalConsequence<A, B> implements BiConstraintConsequence<A, B>,
        BiFunction<A, B, BigDecimal> {

    private final BiConstraintModelNode<A, B> terminalNode;
    private final BiFunction<A, B, BigDecimal> matchWeighter;

    BiConstraintBigDecimalConsequence(BiConstraintModelNode<A, B> terminalNode,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        this.terminalNode = requireNonNull(terminalNode);
        this.matchWeighter = requireNonNull(matchWeighter);
    }

    @Override
    public BiConstraintModelNode<A, B> getTerminalNode() {
        return terminalNode;
    }

    @Override
    public ConsequenceMatchWeightType getMatchWeightType() {
        return ConsequenceMatchWeightType.BIG_DECIMAL;
    }

    @Override
    public BigDecimal apply(A a, B b) {
        return matchWeighter.apply(a, b);
    }

}
