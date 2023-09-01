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

package org.optaplanner.core.api.score;

import java.io.Serializable;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;

/**
 * A Score is result of the score function (AKA fitness function) on a single possible solution.
 *
 * @implSpec
 *           <ul>
 *           <li>Implementations must be immutable,
 *           preferably a Java record or even a primitive record,
 *           if the target JDK permits that.</li>
 *           <li>Implementations must override {@link #initScore()},
 *           or else an endless loop occurs.</li>
 *           <li>Implementations are allowed to optionally implement Pareto comparison
 *           and therefore slightly violate the transitive requirement of {@link Comparable#compareTo(Object)}.</li>
 *           </ul>
 * @param <Score_> the actual score type to allow addition, subtraction and other arithmetic
 * @see HardSoftScore
 */
public interface Score<Score_ extends Score<Score_>>
        extends Comparable<Score_>, Serializable {

    /**
     * The init score is the negative of the number of uninitialized genuine planning variables.
     * If it's 0 (which it usually is), the {@link PlanningSolution} is fully initialized
     * and the score's {@link Object#toString()} does not mention it.
     * <p>
     * During {@link #compareTo(Object)}, it's even more important than the hard score:
     * if you don't want this behaviour, read about overconstrained planning in the reference manual.
     *
     * @return higher is better, always negative (except in statistical calculations), 0 if all planning variables are
     *         initialized
     */
    default int initScore() {
        // TODO remove default implementation in 9.0; exists only for backwards compatibility
        return getInitScore();
    }

    /**
     * As defined by {@link #initScore()}.
     *
     * @deprecated Use {@link #initScore()} instead.
     */
    @Deprecated(forRemoval = true)
    default int getInitScore() {
        return initScore();
    }

    /**
     * For example {@code 0hard/-8soft} with {@code -7} returns {@code -7init/0hard/-8soft}.
     *
     * @param newInitScore always negative (except in statistical calculations), 0 if all planning variables are initialized
     * @return equals score except that {@link #initScore()} is set to {@code newInitScore}
     */
    Score_ withInitScore(int newInitScore);

    /**
     * Returns a Score whose value is (this + addend).
     *
     * @param addend value to be added to this Score
     * @return this + addend
     */
    Score_ add(Score_ addend);

    /**
     * Returns a Score whose value is (this - subtrahend).
     *
     * @param subtrahend value to be subtracted from this Score
     * @return this - subtrahend, rounded as necessary
     */
    Score_ subtract(Score_ subtrahend);

    /**
     * Returns a Score whose value is (this * multiplicand).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double multiplicand
     * should have no impact on the returned scale/precision.
     *
     * @param multiplicand value to be multiplied by this Score.
     * @return this * multiplicand
     */
    Score_ multiply(double multiplicand);

    /**
     * Returns a Score whose value is (this / divisor).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double divisor
     * should have no impact on the returned scale/precision.
     *
     * @param divisor value by which this Score is to be divided
     * @return this / divisor
     */
    Score_ divide(double divisor);

    /**
     * Returns a Score whose value is (this ^ exponent).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}).
     * <p>
     * If the implementation has a scale/precision, then the unspecified scale/precision of the double exponent
     * should have no impact on the returned scale/precision.
     *
     * @param exponent value by which this Score is to be powered
     * @return this ^ exponent
     */
    Score_ power(double exponent);

    /**
     * Returns a Score whose value is (- this).
     *
     * @return - this
     */
    default Score_ negate() {
        Score_ zero = zero();
        Score_ current = (Score_) this;
        if (zero.equals(current)) {
            return current;
        }
        return zero.subtract(current);
    }

    /**
     * Returns a Score whose value is the absolute value of the score, i.e. |this|.
     *
     * @return never null
     */
    Score_ abs();

    /**
     * Returns a Score, all levels of which are zero.
     *
     * @return never null
     */
    Score_ zero();

    /**
     *
     * @return true when this {@link Object#equals(Object) is equal to} {@link #zero()}.
     */
    default boolean isZero() {
        return this.equals(zero());
    }

    /**
     * Returns an array of numbers representing the Score. Each number represents 1 score level.
     * A greater score level uses a lower array index than a lesser score level.
     * <p>
     * When rounding is needed, each rounding should be floored (as defined by {@link Math#floor(double)}).
     * The length of the returned array must be stable for a specific {@link Score} implementation.
     * <p>
     * For example: {@code -0hard/-7soft} returns {@code new int{-0, -7}}
     * <p>
     * The level numbers do not contain the {@link #initScore()}.
     * For example: {@code -3init/-0hard/-7soft} also returns {@code new int{-0, -7}}
     *
     * @return never null
     */
    Number[] toLevelNumbers();

    /**
     * As defined by {@link #toLevelNumbers()}, only returns double[] instead of Number[].
     *
     * @return never null
     */
    default double[] toLevelDoubles() {
        Number[] levelNumbers = toLevelNumbers();
        double[] levelDoubles = new double[levelNumbers.length];
        for (int i = 0; i < levelNumbers.length; i++) {
            levelDoubles[i] = levelNumbers[i].doubleValue();
        }
        return levelDoubles;
    }

    /**
     * Checks if the {@link PlanningSolution} of this score was fully initialized when it was calculated.
     *
     * @return true if {@link #initScore()} is 0
     */
    default boolean isSolutionInitialized() {
        return initScore() >= 0;
    }

    /**
     * A {@link PlanningSolution} is feasible if it has no broken hard constraints
     * and {@link #isSolutionInitialized()} is true.
     *
     * Simple scores ({@link SimpleScore}, {@link SimpleLongScore}, {@link SimpleBigDecimalScore}) are always feasible,
     * if their {@link #initScore()} is 0.
     *
     * @return true if the hard score is 0 or higher and the {@link #initScore()} is 0.
     */
    boolean isFeasible();

    /**
     * Like {@link Object#toString()}, but trims score levels which have a zero weight.
     * For example {@literal 0hard/-258soft} returns {@literal -258soft}.
     * <p>
     * Do not use this format to persist information as text, use {@link Object#toString()} instead,
     * so it can be parsed reliably.
     *
     * @return never null
     */
    String toShortString();

}
