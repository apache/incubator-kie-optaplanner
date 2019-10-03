/*
 *  Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.optaplanner.core.impl.score.stream.tri;

import java.math.BigDecimal;

import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;

public interface InnerTriConstraintStream<A, B, C> extends TriConstraintStream<A, B, C> {

    @Override
    default Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntTriFunction<A, B, C> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongTriFunction<A, B, C> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            TriFunction<A, B, C, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintPackage, String constraintName,
            ToIntTriFunction<A, B, C> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurableLong(String constraintPackage, String constraintName,
            ToLongTriFunction<A, B, C> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurableBigDecimal(String constraintPackage, String constraintName,
            TriFunction<A, B, C, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    default Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntTriFunction<A, B, C> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongTriFunction<A, B, C> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            TriFunction<A, B, C, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurable(String constraintPackage, String constraintName,
            ToIntTriFunction<A, B, C> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurableLong(String constraintPackage, String constraintName,
            ToLongTriFunction<A, B, C> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurableBigDecimal(String constraintPackage, String constraintName,
            TriFunction<A, B, C, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, true);
    }

    Constraint impactScore(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntTriFunction<A, B, C> matchWeigher, boolean positive);

    Constraint impactScoreLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongTriFunction<A, B, C> matchWeigher, boolean positive);

    Constraint impactScoreBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            TriFunction<A, B, C, BigDecimal> matchWeigher, boolean positive);

    Constraint impactScoreConfigurable(String constraintPackage, String constraintName,
            ToIntTriFunction<A, B, C> matchWeigher, boolean positive);

    Constraint impactScoreConfigurableLong(String constraintPackage, String constraintName,
            ToLongTriFunction<A, B, C> matchWeigher, boolean positive);

    Constraint impactScoreConfigurableBigDecimal(String constraintPackage, String constraintName,
            TriFunction<A, B, C, BigDecimal> matchWeigher, boolean positive);


}
