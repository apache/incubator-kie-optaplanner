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

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.BiConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.QuadConstraintModelNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.TriConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.UniConstraintGraphNode;

public interface ConstraintConsequence<Node_ extends ConstraintGraphNode> {

    static <A> UniConstraintConsequence<A> create(UniConstraintGraphNode<A> terminalNode,
            ToIntFunction<A> matchWeighter) {
        return new UniConstraintIntConsequence<>(terminalNode, matchWeighter);
    }

    static <A> UniConstraintConsequence<A> create(UniConstraintGraphNode<A> terminalNode,
            ToLongFunction<A> matchWeighter) {
        return new UniConstraintLongConsequence<>(terminalNode, matchWeighter);
    }

    static <A> UniConstraintConsequence<A> create(UniConstraintGraphNode<A> terminalNode,
            Function<A, BigDecimal> matchWeighter) {
        return new UniConstraintBigDecimalConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B> BiConstraintConsequence<A, B> create(BiConstraintGraphNode<A, B> terminalNode,
            ToIntBiFunction<A, B> matchWeighter) {
        return new BiConstraintIntConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B> BiConstraintConsequence<A, B> create(BiConstraintGraphNode<A, B> terminalNode,
            ToLongBiFunction<A, B> matchWeighter) {
        return new BiConstraintLongConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B> BiConstraintConsequence<A, B> create(BiConstraintGraphNode<A, B> terminalNode,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        return new BiConstraintBigDecimalConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C> TriConstraintConsequence<A, B, C> create(TriConstraintGraphNode<A, B, C> terminalNode,
            ToIntTriFunction<A, B, C> matchWeighter) {
        return new TriConstraintIntConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C> TriConstraintConsequence<A, B, C> create(TriConstraintGraphNode<A, B, C> terminalNode,
            ToLongTriFunction<A, B, C> matchWeighter) {
        return new TriConstraintLongConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C> TriConstraintConsequence<A, B, C> create(TriConstraintGraphNode<A, B, C> terminalNode,
            TriFunction<A, B, C, BigDecimal> matchWeighter) {
        return new TriConstraintBigDecimalConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C, D> QuadConstraintConsequence<A, B, C, D> create(QuadConstraintModelNode<A, B, C, D> terminalNode,
            ToIntQuadFunction<A, B, C, D> matchWeighter) {
        return new QuadConstraintIntConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C, D> QuadConstraintConsequence<A, B, C, D> create(QuadConstraintModelNode<A, B, C, D> terminalNode,
            ToLongQuadFunction<A, B, C, D> matchWeighter) {
        return new QuadConstraintLongConsequence<>(terminalNode, matchWeighter);
    }

    static <A, B, C, D> QuadConstraintConsequence<A, B, C, D> create(QuadConstraintModelNode<A, B, C, D> terminalNode,
            QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        return new QuadConstraintBigDecimalConsequence<>(terminalNode, matchWeighter);
    }

    /**
     * The node on which the consequence will be applied.
     * 
     * @return never null
     */
    Node_ getTerminalNode();

    /**
     * The numeric type of the match weight that score will be impacted with.
     * See Javadoc for extending interfaces for more.
     *
     * @return never null
     */
    ConsequenceMatchWeightType getMatchWeightType();

}
