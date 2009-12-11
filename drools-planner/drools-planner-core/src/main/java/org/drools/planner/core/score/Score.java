package org.drools.planner.core.score;

/**
 * A Score is result of the score function (AKA fitness function) on a single possible solution.
 * <p/>
 * Implementations must be immutable.
 * @see AbstractScore
 * @see DefaultHardAndSoftScore
 * @author Geoffrey De Smet
 */
public interface Score<S extends Score> extends Comparable<S> {

    /**
     * Returns a Score whose value is (this + augend).
     * @param augend value to be added to this Score
     * @return this + augend
     */
    S add(S augend);

    /**
     * Returns a Score whose value is (this - subtrahend).
     * @param subtrahend value to be subtracted from this Score
     * @return this - subtrahend, rounded as necessary
     */
    S substract(S subtrahend);

    /**
     * Returns a Score whose value is (this × multiplicand).
     * Rounding will be applied as needed.
     * @param multiplicand value to be multiplied by this Score.
     * @return this * multiplicand
     */
    S multiply(double  multiplicand);

    /**
     * Returns a Score whose value is (this / divisor).
     * Rounding will be applied as needed.
     * @param divisor value by which this Score is to be divided
     * @return this / divisor
     */
    S divide(double divisor);

}
