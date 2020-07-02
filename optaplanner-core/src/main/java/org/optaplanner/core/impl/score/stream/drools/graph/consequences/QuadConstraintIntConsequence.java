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

package org.optaplanner.core.impl.score.stream.drools.graph.consequences;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.QuadConstraintModelNode;

final class QuadConstraintIntConsequence<A, B, C, D> implements QuadConstraintConsequence<A, B, C, D>,
        Supplier<ToIntQuadFunction<A, B, C, D>> {

    private final QuadConstraintModelNode<A, B, C, D> terminalNode;
    private final ToIntQuadFunction<A, B, C, D> matchWeighter;

    QuadConstraintIntConsequence(QuadConstraintModelNode<A, B, C, D> terminalNode,
            ToIntQuadFunction<A, B, C, D> matchWeighter) {
        this.terminalNode = requireNonNull(terminalNode);
        this.matchWeighter = requireNonNull(matchWeighter);
    }

    @Override
    public QuadConstraintModelNode<A, B, C, D> getTerminalNode() {
        return terminalNode;
    }

    @Override
    public ConsequenceMatchWeightType getMatchWeightType() {
        return ConsequenceMatchWeightType.INTEGER;
    }

    @Override
    public ToIntQuadFunction<A, B, C, D> get() {
        return matchWeighter;
    }
}
