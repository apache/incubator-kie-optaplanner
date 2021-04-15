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

package org.optaplanner.core.impl.score.inliner;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * There are several valid ways how an impacter could be called from a constraint stream:
 *
 * <ul>
 * <li>{@code .penalize(..., (int) 1)}</li>
 * <li>{@code .penalizeLong(..., (int) 1)}</li>
 * <li>{@code .penalizeLong(..., (long) 1)}</li>
 * <li>{@code .penalizeBigDecimal(..., (int) 1)}</li>
 * <li>{@code .penalizeBigDecimal(..., (long) 1)}</li>
 * <li>{@code .penalizeBigDecimal(..., BigDecimal.ONE)}</li>
 * <li>Plus reward variants of the above.</li>
 * </ul>
 *
 * Implementations of this class need to be able to handle all of these, casting to the appropriate type.
 * In certain cases, such as when BigDecimal is passed into an int impacter, methods are allowed to throw exceptions.
 * That state should be considered impossible, ruled out by the CS API itself.
 */
public interface WeightedScoreImpacter {

    static WeightedScoreImpacter of(IntImpactFunction impactFunction) {
        return new IntWeightedScoreImpacter(impactFunction);
    }

    static WeightedScoreImpacter of(LongImpactFunction impactFunction) {
        return new LongWeightedScoreImpacter(impactFunction);
    }

    static WeightedScoreImpacter of(BigDecimalImpactFunction impactFunction) {
        return new BigDecimalWeightedScoreImpacter(impactFunction);
    }

    /**
     * @param matchWeight never null
     * @param justificationsSupplier never null
     * @return never null
     */
    UndoScoreImpacter impactScore(int matchWeight, JustificationsSupplier justificationsSupplier);

    /**
     * @param matchWeight never null
     * @param justificationsSupplier never null
     * @return never null
     */
    UndoScoreImpacter impactScore(long matchWeight, JustificationsSupplier justificationsSupplier);

    /**
     * @param matchWeight never null
     * @param justificationsSupplier never null
     * @return never null
     */
    UndoScoreImpacter impactScore(BigDecimal matchWeight, JustificationsSupplier justificationsSupplier);

    @FunctionalInterface
    interface IntImpactFunction {

        UndoScoreImpacter impact(int matchWeight, JustificationsSupplier justificationsSupplier);

    }

    @FunctionalInterface
    interface LongImpactFunction {

        UndoScoreImpacter impact(long matchWeight, JustificationsSupplier justificationsSupplier);

    }

    @FunctionalInterface
    interface BigDecimalImpactFunction extends BiFunction<BigDecimal, JustificationsSupplier, UndoScoreImpacter> {

    }

}
