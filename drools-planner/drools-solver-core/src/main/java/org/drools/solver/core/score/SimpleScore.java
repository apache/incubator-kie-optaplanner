package org.drools.solver.core.score;

/**
 * A SimpleScore is a Score based on constraints.
 * <p/>
 * Implementations must be immutable.
 * @see Score
 * @see DefaultSimpleScore
 * @author Geoffrey De Smet
 */
public interface SimpleScore extends Score<SimpleScore> {

    /**
     * The total of the broken negative constraints and fulfilled postive hard constraints.
     * Their weight is included in the total.
     * The score is usually a negative number because most use cases only have negative constraints.
     *
     * @return higher is better, usually negative, 0 if no constraints are broken/fulfilled
     */
    int getScore();

}