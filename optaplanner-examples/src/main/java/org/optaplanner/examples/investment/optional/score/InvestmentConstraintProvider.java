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

package org.optaplanner.examples.investment.optional.score;

import java.util.function.Function;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.examples.investment.domain.AssetClassAllocation;
import org.optaplanner.examples.investment.domain.InvestmentParametrization;

public class InvestmentConstraintProvider implements ConstraintProvider {

    private static final String CONSTRAINT_PACKAGE = "org.optaplanner.examples.investment.solver";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                assetsDeviationGreaterThanMaximumPenalty(constraintFactory),
                regionQuantityGreaterThanMaximumPenalty(constraintFactory),
                sectorQuantityGreaterThanMaximumPenalty(constraintFactory),
                assetExpectedReturnReward(constraintFactory)
        };
    }

    private Constraint assetsDeviationGreaterThanMaximumPenalty(ConstraintFactory cf) {
        return cf.from(AssetClassAllocation.class)
                .join(AssetClassAllocation.class)
                .groupBy(ConstraintCollectors.sumLong(AssetClassAllocation::calculateSquaredStandardDeviationFemtosFromTo))
                .join(InvestmentParametrization.class,
                        Joiners.greaterThan(Function.identity(),
                                InvestmentParametrization::getSquaredStandardDeviationFemtosMaximum))
                .penalizeLong(CONSTRAINT_PACKAGE, "Standard deviation maximum", HardSoftLongScore.ONE_HARD, (deviation,
                        parametrization) -> deviation - parametrization.getSquaredStandardDeviationFemtosMaximum());
    }

    private Constraint regionQuantityGreaterThanMaximumPenalty(ConstraintFactory cf) {
        return cf.from(AssetClassAllocation.class)
                .groupBy(AssetClassAllocation::getRegion,
                        ConstraintCollectors.sumLong(AssetClassAllocation::getQuantityMillis))
                .filter((region, totalQuantity) -> totalQuantity > region.getQuantityMillisMaximum())
                .penalizeLong(CONSTRAINT_PACKAGE, "Region quantity maximum", HardSoftLongScore.ONE_HARD,
                        (region, totalQuantity) -> totalQuantity - region.getQuantityMillisMaximum());
    }

    private Constraint sectorQuantityGreaterThanMaximumPenalty(ConstraintFactory cf) {
        return cf.from(AssetClassAllocation.class)
                .groupBy(AssetClassAllocation::getSector,
                        ConstraintCollectors.sumLong(AssetClassAllocation::getQuantityMillis))
                .filter((sector, totalQuantity) -> totalQuantity > sector.getQuantityMillisMaximum())
                .penalizeLong(CONSTRAINT_PACKAGE, "Sector quantity maximum", HardSoftLongScore.ONE_HARD,
                        (sector, totalQuantity) -> totalQuantity - sector.getQuantityMillisMaximum());
    }

    private Constraint assetExpectedReturnReward(ConstraintFactory cf) {
        return cf.from(AssetClassAllocation.class)
                .rewardLong(CONSTRAINT_PACKAGE, "Maximize expected return", HardSoftLongScore.ONE_SOFT,
                        AssetClassAllocation::getQuantifiedExpectedReturnMicros);
    }
}
