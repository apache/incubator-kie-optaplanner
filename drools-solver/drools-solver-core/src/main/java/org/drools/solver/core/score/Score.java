package org.drools.solver.core.score;

/**
 * A Score is result of the score function (AKA fitness function) on a single possible solution.
 * <p/>
 * Implementations must be immutable.
 * @see AbstractScore
 * @see DefaultHardAndSoftScore
 * @author Geoffrey De Smet
 */
public interface Score<S extends Score> extends Comparable<S> {

}
