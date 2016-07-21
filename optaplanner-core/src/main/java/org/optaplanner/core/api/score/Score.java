/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

/**
 * A Score is result of the score function (AKA fitness function) on a single possible solution.
 * <p>
 * Implementations must be immutable.
 * <p>
 * Implementations are allowed to optionally implement Pareto comparison
 * and therefore slightly violate the transitive requirement of {@link Comparable#compareTo(Object)}.
 * <p>
 * An implementation must extend {@link AbstractScore} to ensure backwards compatibility in future versions.
 * @param <S> the actual score type
 * @see AbstractScore
 * @see HardSoftScore
 */
public interface Score<S extends Score> extends Comparable<S> {

    /**
     * The init score is the negative of the number of uninitialized genuine planning variables.
     * If it's 0 (which it usually is), the {@link PlanningSolution} is fully initialized
     * and the score's {@link #toString()} does not mention it.
     * <p>
     * During {@link #compareTo(Object)}, it's even more important than the hard score:
     * if you don't want this behaviour, read about overconstrained planning in the reference manual.
     * @return higher is better, always negative (except in statistical calculations), 0 if all planning variables are initialized
     */
    int getInitScore();

    /**
     * Checks if the {@link PlanningSolution} of this score was fully initialized when it was calculated.
     * @return true if {@link #getInitScore()} is 0
     */
    boolean isSolutionInitialized();

    /**
     * For example {@code -7init/0hard/-8soft} returns {@code 0hard/-8soft}.
     * @return equal score except that {@link #getInitScore()} is {@code 0}.
     */
    S toInitializedScore();

    /**
     * Returns a Score whose value is (this + augment).
     * @param augment value to be added to this Score
     * @return this + augment
     */
    S add(S augment);

    /**
     * Returns a Score whose value is (this - subtrahend).
     * @param subtrahend value to be subtracted from this Score
     * @return this - subtrahend, rounded as necessary
     */
    S subtract(S subtrahend);

    /**
     * Returns a Score whose value is (this * multiplicand).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double multiplicand
     * should have no impact on the returned scale/precision.
     * @param multiplicand value to be multiplied by this Score.
     * @return this * multiplicand
     */
    S multiply(double multiplicand);

    /**
     * Returns a Score whose value is (this / divisor).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double divisor
     * should have no impact on the returned scale/precision.
     * @param divisor value by which this Score is to be divided
     * @return this / divisor
     */
    S divide(double divisor);

    /**
     * Returns a Score whose value is (this ^ exponent).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double exponent
     * should have no impact on the returned scale/precision.
     * @param exponent value by which this Score is to be powered
     * @return this ^ exponent
     */
    S power(double exponent);

    /**
     * Returns a Score whose value is (- this).
     * @return - this
     */
    S negate();

    /**
     * Returns an array of numbers representing the Score. Each number represents 1 score level.
     * A greater score level uses a lower array index than a lesser score level.
     * <p>
     * When rounding is needed, each rounding should be floored (as defined by {@link Math#floor(double)}).
     * The length of the returned array must be stable for a specific {@link Score} implementation.
     * <p>
     * For example: {@code -0hard/-7soft} returns {@code new int{-0, -7}}
     * <p>
     * The level numbers do not contain the {@link #getInitScore()}.
     * For example: {@code -3init/-0hard/-7soft} also returns {@code new int{-0, -7}}
     * @return never null
     * @see ScoreDefinition#fromLevelNumbers(int, Number[])
     */
    Number[] toLevelNumbers();

    /**
     * @param otherScore never null
     * @return true if the otherScore is accepted as a parameter of {@link #add(Score)}, {@link #subtract(Score)}
     * and {@link #compareTo(Object)}.
     */
    boolean isCompatibleArithmeticArgument(Score otherScore);

}
