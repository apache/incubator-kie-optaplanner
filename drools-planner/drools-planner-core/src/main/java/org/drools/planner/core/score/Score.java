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
     * Returns a Score whose value is (this × multiplicand).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}.
     * @param multiplicand value to be multiplied by this Score.
     * @return this * multiplicand
     */
    S multiply(double  multiplicand);

    /**
     * Returns a Score whose value is (this / divisor).
     * When rounding is needed, it should be floored (as defined by {@link Math#floor(double)}.
     * @param divisor value by which this Score is to be divided
     * @return this / divisor
     */
    S divide(double divisor);

}
