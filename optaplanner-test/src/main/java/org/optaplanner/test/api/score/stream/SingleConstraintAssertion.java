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

package org.optaplanner.test.api.score.stream;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.stream.Constraint;

public interface SingleConstraintAssertion {

    /**
     * As defined by {@link #hasNoImpact(String)} with a null message.
     */
    default void hasNoImpact() {
        hasNoImpact(null);
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in neither penalty nor reward.
     *
     * @param message sometimes null, description of the scenario being asserted
     * @throws AssertionError when either a penalty or a reward is observed
     */
    void hasNoImpact(String message);

    /**
     * As defined by {@link #penalizesBy(int, String)} with a null message.
     */
    default void penalizesBy(int matchWeightTotal) {
        penalizesBy(matchWeightTotal, null);
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in a specific penalty.
     *
     * @param matchWeightTotal sum of weights of constraint matches from applying the given facts to the constraint
     * @param message sometimes null, description of the scenario being asserted
     * @throws AssertionError when the expected penalty is not observed
     */
    void penalizesBy(int matchWeightTotal, String message);

    /**
     * As defined by {@link #penalizesBy(int, String)} with a null message.
     */
    default void penalizesBy(long matchWeightTotal) {
        penalizesBy(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #penalizesBy(int, String)}.
     */
    void penalizesBy(long matchWeightTotal, String message);

    /**
     * As defined by {@link #penalizesBy(int, String)} with a null message.
     */
    default void penalizesBy(BigDecimal matchWeightTotal) {
        penalizesBy(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #penalizesBy(int, String)}.
     */
    void penalizesBy(BigDecimal matchWeightTotal, String message);

    /**
     * As defined by {@link #rewardsWith(int, String)} with a null message.
     */
    default void rewardsWith(int matchWeightTotal) {
        rewardsWith(matchWeightTotal, null);
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in a specific reward.
     *
     * @param matchWeightTotal sum of weights of constraint matches from applying the given facts to the constraint
     * @param message sometimes null, description of the scenario being asserted
     * @throws AssertionError when the expected reward is not observed
     */
    void rewardsWith(int matchWeightTotal, String message);

    /**
     * As defined by {@link #rewardsWith(int, String)} with a null message.
     */
    default void rewardsWith(long matchWeightTotal) {
        rewardsWith(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #rewardsWith(int, String)}.
     */
    void rewardsWith(long matchWeightTotal, String message);

    /**
     * As defined by {@link #rewardsWith(int, String)} with a null message.
     */
    default void rewardsWith(BigDecimal matchWeightTotal) {
        rewardsWith(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #rewardsWith(int, String)}.
     */
    void rewardsWith(BigDecimal matchWeightTotal, String message);

}
