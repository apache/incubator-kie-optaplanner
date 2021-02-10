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

package org.optaplanner.core.impl.score.stream.bavet.tri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraint;
import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintFactory;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetNodeBuildPolicy;
import org.optaplanner.core.impl.score.stream.common.ScoreImpactType;
import org.optaplanner.core.impl.score.stream.tri.InnerTriConstraintStream;

public abstract class BavetAbstractTriConstraintStream<Solution_, A, B, C> extends BavetAbstractConstraintStream<Solution_>
        implements InnerTriConstraintStream<A, B, C> {

    protected final List<BavetAbstractTriConstraintStream<Solution_, A, B, C>> childStreamList = new ArrayList<>(2);

    public BavetAbstractTriConstraintStream(BavetConstraintFactory<Solution_> constraintFactory) {
        super(constraintFactory);
    }

    // ************************************************************************
    // Stream builder methods
    // ************************************************************************

    protected void addChildStream(BavetAbstractTriConstraintStream<Solution_, A, B, C> childStream) {
        childStreamList.add(childStream);
    }

    @Override
    public int getCardinality() {
        return 3;
    }

    // ************************************************************************
    // Filter
    // ************************************************************************

    @Override
    public BavetAbstractTriConstraintStream<Solution_, A, B, C> filter(TriPredicate<A, B, C> predicate) {
        BavetFilterTriConstraintStream<Solution_, A, B, C> stream = new BavetFilterTriConstraintStream<>(constraintFactory,
                this, predicate);
        addChildStream(stream);
        return stream;
    }

    // ************************************************************************
    // Join
    // ************************************************************************

    @Override
    public <D> QuadConstraintStream<A, B, C, D> join(UniConstraintStream<D> otherStream, QuadJoiner<A, B, C, D> joiner) {
        throw new UnsupportedOperationException();
    }

    // ************************************************************************
    // If (not) exists
    // ************************************************************************

    @SafeVarargs
    @Override
    public final <D> TriConstraintStream<A, B, C> ifExists(Class<D> otherClass, QuadJoiner<A, B, C, D>... joiners) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    @Override
    public final <D> TriConstraintStream<A, B, C> ifNotExists(Class<D> otherClass, QuadJoiner<A, B, C, D>... joiners) {
        throw new UnsupportedOperationException();
    }

    // ************************************************************************
    // Group by
    // ************************************************************************

    @Override
    public <ResultContainer_, Result_> UniConstraintStream<Result_> groupBy(
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_> BiConstraintStream<ResultA_, ResultB_> groupBy(
            TriConstraintCollector<A, B, C, ResultContainerA_, ResultA_> collectorA,
            TriConstraintCollector<A, B, C, ResultContainerB_, ResultB_> collectorB) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_>
            TriConstraintStream<ResultA_, ResultB_, ResultC_>
            groupBy(TriConstraintCollector<A, B, C, ResultContainerA_, ResultA_> collectorA,
                    TriConstraintCollector<A, B, C, ResultContainerB_, ResultB_> collectorB,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> collectorC) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<ResultA_, ResultB_, ResultC_, ResultD_>
            groupBy(TriConstraintCollector<A, B, C, ResultContainerA_, ResultA_> collectorA,
                    TriConstraintCollector<A, B, C, ResultContainerB_, ResultB_> collectorB,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> collectorC,
                    TriConstraintCollector<A, B, C, ResultContainerD_, ResultD_> collectorD) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_> UniConstraintStream<GroupKey_> groupBy(TriFunction<A, B, C, GroupKey_> groupKeyMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_>
            TriConstraintStream<GroupKey_, ResultB_, ResultC_> groupBy(TriFunction<A, B, C, GroupKey_> groupKeyMapping,
                    TriConstraintCollector<A, B, C, ResultContainerB_, ResultB_> collectorB,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> collectorC) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKey_, ResultB_, ResultC_, ResultD_>
            groupBy(TriFunction<A, B, C, GroupKey_> groupKeyMapping,
                    TriConstraintCollector<A, B, C, ResultContainerB_, ResultB_> collectorB,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> collectorC,
                    TriConstraintCollector<A, B, C, ResultContainerD_, ResultD_> collectorD) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_, ResultContainer_, Result_> BiConstraintStream<GroupKey_, Result_> groupBy(
            TriFunction<A, B, C, GroupKey_> groupKeyMapping,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKeyA_, GroupKeyB_> BiConstraintStream<GroupKeyA_, GroupKeyB_> groupBy(
            TriFunction<A, B, C, GroupKeyA_> groupKeyAMapping, TriFunction<A, B, C, GroupKeyB_> groupKeyBMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, ResultContainer_, Result_> TriConstraintStream<GroupKeyA_, GroupKeyB_, Result_> groupBy(
            TriFunction<A, B, C, GroupKeyA_> groupKeyAMapping, TriFunction<A, B, C, GroupKeyB_> groupKeyBMapping,
            TriConstraintCollector<A, B, C, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    TriFunction<A, B, C, GroupKeyA_> groupKeyAMapping, TriFunction<A, B, C, GroupKeyB_> groupKeyBMapping,
                    TriConstraintCollector<A, B, C, ResultContainerC_, ResultC_> collectorC,
                    TriConstraintCollector<A, B, C, ResultContainerD_, ResultD_> collectorD) {
        throw new UnsupportedOperationException();
    }

    // ************************************************************************
    // Penalize/reward
    // ************************************************************************

    @Override
    public final Constraint impactScore(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScore(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntTriFunction<A, B, C> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreLong(String constraintPackage, String constraintName,
            Score<?> constraintWeight, ToLongTriFunction<A, B, C> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreBigDecimal(String constraintPackage, String constraintName,
            Score<?> constraintWeight, TriFunction<A, B, C, BigDecimal> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreConfigurable(String constraintPackage, String constraintName,
            ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreConfigurable(String constraintPackage, String constraintName,
            ToIntTriFunction<A, B, C> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreConfigurableLong(String constraintPackage, String constraintName,
            ToLongTriFunction<A, B, C> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    public final Constraint impactScoreConfigurableBigDecimal(String constraintPackage, String constraintName,
            TriFunction<A, B, C, BigDecimal> matchWeigher, ScoreImpactType impactType) {
        BavetConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                impactType);
        BavetScoringTriConstraintStream<Solution_, A, B, C> stream = new BavetScoringTriConstraintStream<>(constraintFactory,
                this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    public BavetAbstractTriNode<A, B, C> createNodeChain(BavetNodeBuildPolicy<Solution_> buildPolicy,
            Score<?> constraintWeight, BavetAbstractTriNode<A, B, C> parentNode) {
        BavetAbstractTriNode<A, B, C> node = createNode(buildPolicy, constraintWeight, parentNode);
        node = processNode(buildPolicy, parentNode, node);
        createChildNodeChains(buildPolicy, constraintWeight, node);
        return node;
    }

    protected BavetAbstractTriNode<A, B, C> processNode(BavetNodeBuildPolicy<Solution_> buildPolicy,
            BavetAbstractTriNode<A, B, C> parentNode, BavetAbstractTriNode<A, B, C> node) {
        BavetAbstractTriNode<A, B, C> sharedNode = buildPolicy.retrieveSharedNode(node);
        if (sharedNode != node) { // Share node
            return sharedNode;
        }
        if (parentNode != null) { // TODO remove null check and don't go through this for from and joins
            parentNode.addChildNode(node);
        }
        return node;
    }

    protected void createChildNodeChains(BavetNodeBuildPolicy<Solution_> buildPolicy, Score<?> constraintWeight,
            BavetAbstractTriNode<A, B, C> node) {
        if (childStreamList.isEmpty()) {
            throw new IllegalStateException("The stream (" + this + ") leads to nowhere.\n"
                    + "Maybe don't create it.");
        }
        for (BavetAbstractTriConstraintStream<Solution_, A, B, C> childStream : childStreamList) {
            childStream.createNodeChain(buildPolicy, constraintWeight, node);
        }
    }

    protected abstract BavetAbstractTriNode<A, B, C> createNode(BavetNodeBuildPolicy<Solution_> buildPolicy,
            Score<?> constraintWeight, BavetAbstractTriNode<A, B, C> parentNode);

}
