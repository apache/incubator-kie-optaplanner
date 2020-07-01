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

import java.util.function.ToLongFunction;

import org.optaplanner.core.impl.score.stream.drools.model.nodes.UniConstraintModelNode;

final class UniConstraintLongConsequence<A> implements UniConstraintConsequence<A>, ToLongFunction<A> {

    private final UniConstraintModelNode<A> terminalNode;
    private final ToLongFunction<A> matchWeighter;

    UniConstraintLongConsequence(UniConstraintModelNode<A> terminalNode, ToLongFunction<A> matchWeighter) {
        this.terminalNode = requireNonNull(terminalNode);
        this.matchWeighter = requireNonNull(matchWeighter);
    }

    @Override
    public UniConstraintModelNode<A> getTerminalNode() {
        return terminalNode;
    }

    @Override
    public ConsequenceMatchWeightType getMatchWeightType() {
        return ConsequenceMatchWeightType.LONG;
    }

    @Override
    public long applyAsLong(A a) {
        return matchWeighter.applyAsLong(a);
    }
}
