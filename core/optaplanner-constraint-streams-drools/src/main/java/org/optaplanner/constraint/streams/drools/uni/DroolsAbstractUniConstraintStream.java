/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.drools.uni;

import static org.optaplanner.constraint.streams.common.RetrievalSemantics.STANDARD;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.constraint.streams.common.RetrievalSemantics;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.constraint.streams.common.bi.BiJoinerComber;
import org.optaplanner.constraint.streams.common.uni.InnerUniConstraintStream;
import org.optaplanner.constraint.streams.common.uni.UniConstraintBuilderImpl;
import org.optaplanner.constraint.streams.drools.DroolsConstraintFactory;
import org.optaplanner.constraint.streams.drools.bi.DroolsAbstractBiConstraintStream;
import org.optaplanner.constraint.streams.drools.bi.DroolsGroupingBiConstraintStream;
import org.optaplanner.constraint.streams.drools.bi.DroolsJoinBiConstraintStream;
import org.optaplanner.constraint.streams.drools.common.DroolsAbstractConstraintStream;
import org.optaplanner.constraint.streams.drools.common.RuleBuilder;
import org.optaplanner.constraint.streams.drools.common.UniLeftHandSide;
import org.optaplanner.constraint.streams.drools.quad.DroolsGroupingQuadConstraintStream;
import org.optaplanner.constraint.streams.drools.tri.DroolsGroupingTriConstraintStream;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.DefaultConstraintJustification;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintBuilder;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;

public abstract class DroolsAbstractUniConstraintStream<Solution_, A>
        extends DroolsAbstractConstraintStream<Solution_, UniLeftHandSide<A>>
        implements InnerUniConstraintStream<A> {

    public DroolsAbstractUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            RetrievalSemantics retrievalSemantics) {
        super(constraintFactory, retrievalSemantics);
    }

    @Override
    public UniConstraintStream<A> filter(Predicate<A> predicate) {
        DroolsFilterUniConstraintStream<Solution_, A> stream = new DroolsFilterUniConstraintStream<>(constraintFactory,
                this, predicate);
        addChildStream(stream);
        return stream;
    }

    @Override
    @SafeVarargs
    public final <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream,
            BiJoiner<A, B>... joiners) {
        BiJoinerComber<A, B> joinerComber = BiJoinerComber.comb(joiners);
        return join(otherStream, joinerComber);
    }

    @Override
    public final <B> BiConstraintStream<A, B> join(UniConstraintStream<B> otherStream,
            BiJoinerComber<A, B> joinerComber) {
        DroolsAbstractUniConstraintStream<Solution_, B> castOtherStream =
                (DroolsAbstractUniConstraintStream<Solution_, B>) otherStream;
        DroolsAbstractBiConstraintStream<Solution_, A, B> stream = new DroolsJoinBiConstraintStream<>(constraintFactory,
                this, castOtherStream, joinerComber.getMergedJoiner());
        addChildStream(stream);
        castOtherStream.addChildStream(stream);
        if (joinerComber.getMergedFiltering() == null) {
            return stream;
        } else {
            return stream.filter(joinerComber.getMergedFiltering());
        }
    }

    @SafeVarargs
    @Override
    public final <B> UniConstraintStream<A> ifExists(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(true, getRetrievalSemantics() != STANDARD, otherClass, joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniConstraintStream<A> ifExistsIncludingNullVars(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(true, true, otherClass, joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniConstraintStream<A> ifNotExists(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(false, getRetrievalSemantics() != STANDARD,
                otherClass, joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniConstraintStream<A> ifNotExistsIncludingNullVars(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(false, true, otherClass, joiners);
    }

    @SafeVarargs
    private <B> UniConstraintStream<A> ifExistsOrNot(boolean shouldExist, boolean shouldIncludeNullVars,
            Class<B> otherClass, BiJoiner<A, B>... joiners) {
        getConstraintFactory().assertValidFromType(otherClass);
        DroolsExistsUniConstraintStream<Solution_, A> stream = new DroolsExistsUniConstraintStream<>(constraintFactory, this,
                shouldExist, shouldIncludeNullVars, otherClass, joiners);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultContainer_, Result_> UniConstraintStream<Result_> groupBy(
            UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        DroolsGroupingUniConstraintStream<Solution_, Result_> stream = new DroolsGroupingUniConstraintStream<>(
                constraintFactory, this, collector);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_> BiConstraintStream<ResultA_, ResultB_> groupBy(
            UniConstraintCollector<A, ResultContainerA_, ResultA_> collectorA,
            UniConstraintCollector<A, ResultContainerB_, ResultB_> collectorB) {
        DroolsGroupingBiConstraintStream<Solution_, ResultA_, ResultB_> stream =
                new DroolsGroupingBiConstraintStream<>(constraintFactory, this, collectorA, collectorB);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_>
            TriConstraintStream<ResultA_, ResultB_, ResultC_>
            groupBy(UniConstraintCollector<A, ResultContainerA_, ResultA_> collectorA,
                    UniConstraintCollector<A, ResultContainerB_, ResultB_> collectorB,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC) {
        DroolsGroupingTriConstraintStream<Solution_, ResultA_, ResultB_, ResultC_> stream =
                new DroolsGroupingTriConstraintStream<>(constraintFactory, this, collectorA, collectorB, collectorC);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultContainerA_, ResultA_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<ResultA_, ResultB_, ResultC_, ResultD_>
            groupBy(UniConstraintCollector<A, ResultContainerA_, ResultA_> collectorA,
                    UniConstraintCollector<A, ResultContainerB_, ResultB_> collectorB,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> collectorD) {
        DroolsGroupingQuadConstraintStream<Solution_, ResultA_, ResultB_, ResultC_, ResultD_> stream =
                new DroolsGroupingQuadConstraintStream<>(constraintFactory, this, collectorA, collectorB, collectorC,
                        collectorD);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKey_> UniConstraintStream<GroupKey_> groupBy(Function<A, GroupKey_> groupKeyMapping) {
        DroolsGroupingUniConstraintStream<Solution_, GroupKey_> stream = new DroolsGroupingUniConstraintStream<>(
                constraintFactory, this, groupKeyMapping);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKey_, ResultContainer_, Result_> BiConstraintStream<GroupKey_, Result_> groupBy(
            Function<A, GroupKey_> groupKeyMapping, UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        DroolsGroupingBiConstraintStream<Solution_, GroupKey_, Result_> stream = new DroolsGroupingBiConstraintStream<>(
                constraintFactory, this, groupKeyMapping, collector);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKey_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_>
            TriConstraintStream<GroupKey_, ResultB_, ResultC_> groupBy(Function<A, GroupKey_> groupKeyMapping,
                    UniConstraintCollector<A, ResultContainerB_, ResultB_> collectorB,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC) {
        DroolsGroupingTriConstraintStream<Solution_, GroupKey_, ResultB_, ResultC_> stream =
                new DroolsGroupingTriConstraintStream<>(constraintFactory, this, groupKeyMapping, collectorB, collectorC);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKey_, ResultContainerB_, ResultB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKey_, ResultB_, ResultC_, ResultD_>
            groupBy(Function<A, GroupKey_> groupKeyMapping, UniConstraintCollector<A, ResultContainerB_, ResultB_> collectorB,
                    UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> collectorD) {
        DroolsGroupingQuadConstraintStream<Solution_, GroupKey_, ResultB_, ResultC_, ResultD_> stream =
                new DroolsGroupingQuadConstraintStream<>(constraintFactory, this, groupKeyMapping, collectorB, collectorC,
                        collectorD);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_> BiConstraintStream<GroupKeyA_, GroupKeyB_> groupBy(
            Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping) {
        DroolsGroupingBiConstraintStream<Solution_, GroupKeyA_, GroupKeyB_> stream = new DroolsGroupingBiConstraintStream<>(
                constraintFactory, this, groupKeyAMapping,
                groupKeyBMapping);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, ResultContainer_, Result_> TriConstraintStream<GroupKeyA_, GroupKeyB_, Result_> groupBy(
            Function<A, GroupKeyA_> groupKeyAMapping,
            Function<A, GroupKeyB_> groupKeyBMapping, UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        DroolsGroupingTriConstraintStream<Solution_, GroupKeyA_, GroupKeyB_, Result_> stream =
                new DroolsGroupingTriConstraintStream<>(constraintFactory, this, groupKeyAMapping, groupKeyBMapping, collector);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, ResultContainerC_, ResultC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> groupBy(
                    Function<A, GroupKeyA_> groupKeyAMapping,
                    Function<A, GroupKeyB_> groupKeyBMapping, UniConstraintCollector<A, ResultContainerC_, ResultC_> collectorC,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> collectorD) {
        DroolsGroupingQuadConstraintStream<Solution_, GroupKeyA_, GroupKeyB_, ResultC_, ResultD_> stream =
                new DroolsGroupingQuadConstraintStream<>(constraintFactory, this, groupKeyAMapping, groupKeyBMapping,
                        collectorC, collectorD);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, GroupKeyC_> TriConstraintStream<GroupKeyA_, GroupKeyB_, GroupKeyC_> groupBy(
            Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping,
            Function<A, GroupKeyC_> groupKeyCMapping) {
        DroolsGroupingTriConstraintStream<Solution_, GroupKeyA_, GroupKeyB_, GroupKeyC_> stream =
                new DroolsGroupingTriConstraintStream<>(constraintFactory, this, groupKeyAMapping, groupKeyBMapping,
                        groupKeyCMapping);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, GroupKeyC_, ResultContainerD_, ResultD_>
            QuadConstraintStream<GroupKeyA_, GroupKeyB_, GroupKeyC_, ResultD_> groupBy(Function<A, GroupKeyA_> groupKeyAMapping,
                    Function<A, GroupKeyB_> groupKeyBMapping, Function<A, GroupKeyC_> groupKeyCMapping,
                    UniConstraintCollector<A, ResultContainerD_, ResultD_> collectorD) {
        DroolsGroupingQuadConstraintStream<Solution_, GroupKeyA_, GroupKeyB_, GroupKeyC_, ResultD_> stream =
                new DroolsGroupingQuadConstraintStream<>(constraintFactory, this, groupKeyAMapping, groupKeyBMapping,
                        groupKeyCMapping, collectorD);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, GroupKeyC_, GroupKeyD_> QuadConstraintStream<GroupKeyA_, GroupKeyB_, GroupKeyC_, GroupKeyD_>
            groupBy(Function<A, GroupKeyA_> groupKeyAMapping, Function<A, GroupKeyB_> groupKeyBMapping,
                    Function<A, GroupKeyC_> groupKeyCMapping, Function<A, GroupKeyD_> groupKeyDMapping) {
        DroolsGroupingQuadConstraintStream<Solution_, GroupKeyA_, GroupKeyB_, GroupKeyC_, GroupKeyD_> stream =
                new DroolsGroupingQuadConstraintStream<>(constraintFactory, this, groupKeyAMapping, groupKeyBMapping,
                        groupKeyCMapping, groupKeyDMapping);
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultA_> UniConstraintStream<ResultA_> map(Function<A, ResultA_> mapping) {
        DroolsMappingUniConstraintStream<Solution_, ResultA_> stream =
                new DroolsMappingUniConstraintStream<>(constraintFactory, this, Objects.requireNonNull(mapping));
        addChildStream(stream);
        return stream;
    }

    @Override
    public <ResultA_> UniConstraintStream<ResultA_> flattenLast(Function<A, Iterable<ResultA_>> mapping) {
        DroolsFlatteningUniConstraintStream<Solution_, ResultA_> stream =
                new DroolsFlatteningUniConstraintStream<>(constraintFactory, this, Objects.requireNonNull(mapping));
        addChildStream(stream);
        return stream;
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public <Score_ extends Score<Score_>> UniConstraintBuilder<A, Score_> innerImpact(Score_ constraintWeight,
            ToIntFunction<A> matchWeigher, ScoreImpactType scoreImpactType) {
        RuleBuilder<Solution_> ruleBuilder = createLeftHandSide().andTerminate(matchWeigher);
        return newTerminator(ruleBuilder, constraintWeight, scoreImpactType);
    }

    private <Score_ extends Score<Score_>> UniConstraintBuilderImpl<A, Score_> newTerminator(RuleBuilder<Solution_> ruleBuilder,
            Score_ constraintWeight, ScoreImpactType impactType) {
        return new UniConstraintBuilderImpl<>(
                (constraintPackage, constraintName, constraintWeight_, impactType_, justificationMapping,
                        indictedObjectsMapping) -> buildConstraint(constraintPackage, constraintName, constraintWeight_,
                                impactType_, justificationMapping, indictedObjectsMapping, ruleBuilder),
                impactType, constraintWeight);
    }

    @Override
    public <Score_ extends Score<Score_>> UniConstraintBuilder<A, Score_> innerImpact(Score_ constraintWeight,
            ToLongFunction<A> matchWeigher, ScoreImpactType scoreImpactType) {
        RuleBuilder<Solution_> ruleBuilder = createLeftHandSide().andTerminate(matchWeigher);
        return newTerminator(ruleBuilder, constraintWeight, scoreImpactType);
    }

    @Override
    public <Score_ extends Score<Score_>> UniConstraintBuilder<A, Score_> innerImpact(Score_ constraintWeight,
            Function<A, BigDecimal> matchWeigher, ScoreImpactType scoreImpactType) {
        RuleBuilder<Solution_> ruleBuilder = createLeftHandSide().andTerminate(matchWeigher);
        return newTerminator(ruleBuilder, constraintWeight, scoreImpactType);
    }

    @Override
    protected final BiFunction<A, Score<?>, DefaultConstraintJustification> getDefaultJustificationMapping() {
        return InnerUniConstraintStream.createDefaultJustificationMapping();
    }

    @Override
    protected final Function<A, Collection<?>> getDefaultIndictedObjectsMapping() {
        return InnerUniConstraintStream.createDefaultIndictedObjectsMapping();
    }

}
