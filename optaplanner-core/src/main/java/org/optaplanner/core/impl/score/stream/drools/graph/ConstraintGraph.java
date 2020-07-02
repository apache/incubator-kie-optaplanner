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

package org.optaplanner.core.impl.score.stream.drools.graph;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.BiConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.QuadConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.TriConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.UniConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelChildNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.BiConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ChildNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.FromNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.QuadConstraintModelNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.TriConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.UniConstraintGraphChildNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.UniConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

public final class ConstraintGraph {

    private final Map<Class, FromNode> fromNodeMap = new LinkedHashMap<>(0);
    private final Set<ConstraintGraphNode> nodeSet = new LinkedHashSet<>(0);
    private final Set<ConstraintConsequence> consequenceSet = new LinkedHashSet<>(0);

    public <A> UniConstraintGraphNode<A> from(Class<A> clz) {
        FromNode<A> node = fromNodeMap.computeIfAbsent(clz, FromNode::new);
        nodeSet.add(node);
        return node;
    }

    public <A> UniConstraintGraphChildNode<A> filter(UniConstraintGraphNode<A> parent, Predicate<A> predicate) {
        return addNode(() -> ConstraintGraphNode.filter(predicate), parent);
    }

    public <A, B> BiConstraintGraphNode<A, B> filter(BiConstraintGraphNode<A, B> parent, BiPredicate<A, B> predicate) {
        return addNode(() -> ConstraintGraphNode.filter(predicate), parent);
    }

    public <A, B, C> TriConstraintGraphNode<A, B, C> filter(TriConstraintGraphNode<A, B, C> parent,
            TriPredicate<A, B, C> predicate) {
        return addNode(() -> ConstraintGraphNode.filter(predicate), parent);
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> filter(QuadConstraintModelNode<A, B, C, D> parent,
            QuadPredicate<A, B, C, D> predicate) {
        return addNode(() -> ConstraintGraphNode.filter(predicate), parent);
    }

    public <A, B> BiConstraintGraphNode<A, B> join(UniConstraintGraphNode<A> leftParent,
            UniConstraintGraphNode<B> rightParent, AbstractBiJoiner<A, B> joiner) {
        return addNode(() -> ConstraintGraphNode.join(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C> TriConstraintGraphNode<A, B, C> join(BiConstraintGraphNode<A, B> leftParent,
            UniConstraintGraphNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        return addNode(() -> ConstraintGraphNode.join(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C, D> QuadConstraintModelNode<A, B, C, D> join(TriConstraintGraphNode<A, B, C> leftParent,
            UniConstraintGraphNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        return addNode(() -> ConstraintGraphNode.join(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B> UniConstraintGraphChildNode<A> ifExists(UniConstraintGraphNode<A> leftParent,
            UniConstraintGraphNode<A> rightParent, AbstractBiJoiner<A, B> joiner) {
        return addNode(() -> ConstraintGraphNode.ifExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C> BiConstraintGraphNode<A, B> ifExists(BiConstraintGraphNode<A, B> leftParent,
            UniConstraintGraphNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        return addNode(() -> ConstraintGraphNode.ifExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C, D> TriConstraintGraphNode<A, B, C> ifExists(TriConstraintGraphNode<A, B, C> leftParent,
            UniConstraintGraphNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        return addNode(() -> ConstraintGraphNode.ifExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C, D, E> QuadConstraintModelNode<A, B, C, D> ifExists(QuadConstraintModelNode<A, B, C, D> leftParent,
            UniConstraintGraphNode<E> rightParent, AbstractPentaJoiner<A, B, C, D, E> joiner) {
        return addNode(() -> ConstraintGraphNode.ifExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B> UniConstraintGraphChildNode<A> ifNotExists(UniConstraintGraphNode<A> leftParent,
            UniConstraintGraphNode<A> rightParent, AbstractBiJoiner<A, B> joiner) {
        return addNode(() -> ConstraintGraphNode.ifNotExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C> BiConstraintGraphNode<A, B> ifNotExists(BiConstraintGraphNode<A, B> leftParent,
            UniConstraintGraphNode<C> rightParent, AbstractTriJoiner<A, B, C> joiner) {
        return addNode(() -> ConstraintGraphNode.ifNotExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C, D> TriConstraintGraphNode<A, B, C> ifNotExists(TriConstraintGraphNode<A, B, C> leftParent,
            UniConstraintGraphNode<D> rightParent, AbstractQuadJoiner<A, B, C, D> joiner) {
        return addNode(() -> ConstraintGraphNode.ifNotExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, B, C, D, E> QuadConstraintModelNode<A, B, C, D> ifNotExists(QuadConstraintModelNode<A, B, C, D> leftParent,
            UniConstraintGraphNode<E> rightParent, AbstractPentaJoiner<A, B, C, D, E> joiner) {
        return addNode(() -> ConstraintGraphNode.ifNotExists(rightParent.getFactType(), joiner), leftParent, rightParent);
    }

    public <A, GroupKey_> UniConstraintGraphChildNode<GroupKey_> groupBy(UniConstraintGraphNode<A> parent,
            Function<A, GroupKey_> mapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping), parent);
    }

    public <A, GroupKeyA_, GroupKeyB_> BiConstraintGraphNode<GroupKeyA_, GroupKeyB_> groupBy(
            UniConstraintGraphNode<A> parent, Function<A, GroupKeyA_> aMapping, Function<A, GroupKeyB_> bMapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping), parent);
    }

    public <A, ResultContainer_, Result_> UniConstraintGraphChildNode<Result_> groupBy(UniConstraintGraphNode<A> parent,
            UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(collector), parent);
    }

    public <A, GroupKey_, ResultContainer_, Result_> BiConstraintGraphNode<GroupKey_, Result_> groupBy(
            UniConstraintGraphNode<A> parent, Function<A, GroupKey_> mapping,
            UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping, collector), parent);
    }

    public <A, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintGraphNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(UniConstraintGraphNode<A> parent,
                    Function<A, GroupKeyA_> aMapping, Function<A, GroupKeyB_> bMapping,
                    UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, collector), parent);
    }

    public <A, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    UniConstraintGraphNode<A> parent, Function<A, GroupKeyA_> aMapping,
                    Function<A, GroupKeyB_> bMapping, UniConstraintCollector<A, ResultContainerC_, ResultC_> cCollector,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> dCollector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, cCollector, dCollector), parent);
    }

    public <A, B, ResultContainer_, Result_> UniConstraintGraphChildNode<Result_> groupBy(
            BiConstraintGraphNode<A, B> parent, BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(collector), parent);
    }

    public <A, B, GroupKey_> UniConstraintGraphChildNode<GroupKey_> groupBy(BiConstraintGraphNode<A, B> parent,
            BiFunction<A, B, GroupKey_> mapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping), parent);
    }

    public <A, B, GroupKey_, ResultContainer_, Result_> BiConstraintGraphNode<GroupKey_, Result_> groupBy(
            BiConstraintGraphNode<A, B> parent, BiFunction<A, B, GroupKey_> mapping,
            BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping, collector), parent);
    }

    public <A, B, GroupKeyA_, GroupKeyB_> BiConstraintGraphNode<GroupKeyA_, GroupKeyB_> groupBy(
            BiConstraintGraphNode<A, B> parent, BiFunction<A, B, GroupKeyA_> aMapping,
            BiFunction<A, B, GroupKeyB_> bMapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping), parent);
    }

    public <A, B, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintGraphNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(BiConstraintGraphNode<A, B> parent,
                    BiFunction<A, B, GroupKeyA_> aMapping, BiFunction<A, B, GroupKeyB_> bMapping,
                    BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, collector), parent);
    }

    public <A, B, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(BiConstraintGraphNode<A, B> parent,
                    BiFunction<A, B, GroupKeyA_> aMapping, BiFunction<A, B, GroupKeyB_> bMapping,
                    BiConstraintCollector<A, B, ResultContainerC_, ResultC_> cCollector,
                    BiConstraintCollector<A, B, ResultContainerD_, ResultD_> dCollector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, cCollector, dCollector), parent);
    }

    public <A, B, C, ResultContainer_, Result_> UniConstraintGraphChildNode<Result_> groupBy(
            TriConstraintGraphNode<A, B, C> parent,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(collector), parent);
    }

    public <A, B, C, GroupKey_> UniConstraintGraphChildNode<GroupKey_> groupBy(TriConstraintGraphNode<A, B, C> parent,
            TriFunction<A, B, C, GroupKey_> mapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping), parent);
    }

    public <A, B, C, GroupKey_, ResultContainer_, Result_> BiConstraintGraphNode<GroupKey_, Result_> groupBy(
            TriConstraintGraphNode<A, B, C> parent, TriFunction<A, B, C, GroupKey_> mapping,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping, collector), parent);
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_> BiConstraintGraphNode<GroupKeyA_, GroupKeyB_> groupBy(
            TriConstraintGraphNode<A, B, C> parent, TriFunction<A, B, C, GroupKeyA_> aMapping,
            TriFunction<A, B, C, GroupKeyB_> bMapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping), parent);
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintGraphNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(TriConstraintGraphNode<A, B, C> parent,
                    TriFunction<A, B, C, GroupKeyA_> aMapping, TriFunction<A, B, C, GroupKeyB_> bMapping,
                    TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, collector), parent);
    }

    public <A, B, C, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    TriConstraintGraphNode<A, B, C> parent, TriFunction<A, B, C, GroupKeyA_> aMapping,
                    TriFunction<A, B, C, GroupKeyB_> bMapping,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> cCollector,
                    TriConstraintCollector<A, B, C, ResultContainerD_, ResultD_> dCollector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, cCollector, dCollector), parent);
    }

    public <A, B, C, D, ResultContainer_, Result_> UniConstraintGraphChildNode<Result_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(collector), parent);
    }

    public <A, B, C, D, GroupKey_> UniConstraintGraphChildNode<GroupKey_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKey_> mapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping), parent);
    }

    public <A, B, C, D, GroupKey_, ResultContainer_, Result_> BiConstraintGraphNode<GroupKey_, Result_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKey_> mapping,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(mapping, collector), parent);
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_> BiConstraintGraphNode<GroupKeyA_, GroupKeyB_> groupBy(
            QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKeyA_> aMapping,
            QuadFunction<A, B, C, D, GroupKeyB_> bMapping) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping), parent);
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_, ResultContainer_, Result_>
            TriConstraintGraphNode<GroupKeyA_, GroupKeyB_, Result_> groupBy(QuadConstraintModelNode<A, B, C, D> parent,
                    QuadFunction<A, B, C, D, GroupKeyA_> aMapping, QuadFunction<A, B, C, D, GroupKeyB_> bMapping,
                    QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, collector), parent);
    }

    public <A, B, C, D, GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintModelNode<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    QuadConstraintModelNode<A, B, C, D> parent, QuadFunction<A, B, C, D, GroupKeyA_> aMapping,
                    QuadFunction<A, B, C, D, GroupKeyB_> bMapping,
                    QuadConstraintCollector<A, B, C, D, ResultContainerC_, ResultC_> cCollector,
                    QuadConstraintCollector<A, B, C, D, ResultContainerD_, ResultD_> dCollector) {
        return addNode(() -> ConstraintGraphNode.groupBy(aMapping, bMapping, cCollector, dCollector), parent);
    }

    private <Node_ extends ConstraintGraphNode & ChildNode> Node_ addNode(Supplier<Node_> nodeSupplier,
            ConstraintGraphNode... parentNodes) {
        Node_ node = nodeSupplier.get();
        AbstractConstraintModelChildNode castChildNode = (AbstractConstraintModelChildNode) node;
        for (ConstraintGraphNode parentNode : parentNodes) {
            castChildNode.addParentNode(parentNode);
            AbstractConstraintModelNode castNode = (AbstractConstraintModelChildNode) parentNode;
            castNode.addChildNode(castChildNode);
        }
        return node;
    }

    public <A> UniConstraintConsequence<A> impact(UniConstraintGraphNode<A> parent) {
        return impact(() -> ConstraintConsequence.create(parent, (ToIntFunction<A>) a -> 1));
    }

    public <A> UniConstraintConsequence<A> impact(UniConstraintGraphNode<A> parent, ToIntFunction<A> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A> UniConstraintConsequence<A> impact(UniConstraintGraphNode<A> parent, ToLongFunction<A> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A> UniConstraintConsequence<A> impact(UniConstraintGraphNode<A> parent,
            Function<A, BigDecimal> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B> BiConstraintConsequence<A, B> impact(BiConstraintGraphNode<A, B> parent) {
        return impact(() -> ConstraintConsequence.create(parent, (ToIntBiFunction<A, B>) (a, b) -> 1));
    }

    public <A, B> BiConstraintConsequence<A, B> impact(BiConstraintGraphNode<A, B> parent,
            ToIntBiFunction<A, B> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B> BiConstraintConsequence<A, B> impact(BiConstraintGraphNode<A, B> parent,
            ToLongBiFunction<A, B> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B> BiConstraintConsequence<A, B> impact(BiConstraintGraphNode<A, B> parent,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C> TriConstraintConsequence<A, B, C> impact(TriConstraintGraphNode<A, B, C> parent) {
        return impact(() -> ConstraintConsequence.create(parent, (ToIntTriFunction<A, B, C>) (a, b, c) -> 1));
    }

    public <A, B, C> TriConstraintConsequence<A, B, C> impact(TriConstraintGraphNode<A, B, C> parent,
            ToIntTriFunction<A, B, C> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C> TriConstraintConsequence<A, B, C> impact(TriConstraintGraphNode<A, B, C> parent,
            ToLongTriFunction<A, B, C> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C> TriConstraintConsequence<A, B, C> impact(TriConstraintGraphNode<A, B, C> parent,
            TriFunction<A, B, C, BigDecimal> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C, D> QuadConstraintConsequence<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent) {
        return impact(() -> ConstraintConsequence.create(parent, (ToIntQuadFunction<A, B, C, D>) (a, b, c, d) -> 1));
    }

    public <A, B, C, D> QuadConstraintConsequence<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            ToIntQuadFunction<A, B, C, D> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C, D> QuadConstraintConsequence<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            ToLongQuadFunction<A, B, C, D> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    public <A, B, C, D> QuadConstraintConsequence<A, B, C, D> impact(QuadConstraintModelNode<A, B, C, D> parent,
            QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        return impact(() -> ConstraintConsequence.create(parent, matchWeighter));
    }

    private <Node_ extends ConstraintGraphNode, Consequence_ extends ConstraintConsequence<Node_>> Consequence_
            impact(Supplier<Consequence_> consequenceSupplier) {
        Consequence_ consequence = consequenceSupplier.get();
        consequenceSet.add(consequence);
        return consequence;
    }

    public Set<FromNode> getFromNodes() {
        return Collections.unmodifiableSet(new HashSet<>(fromNodeMap.values()));
    }

    public Set<ConstraintConsequence> getConsequences() {
        return Collections.unmodifiableSet(consequenceSet);
    }

}
