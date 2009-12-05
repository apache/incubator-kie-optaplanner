package org.drools.solver.core.score.definition;

import java.io.Serializable;

import org.drools.solver.core.score.Score;

/**
 * Abstract superclass for {@link ScoreDefinition}.
 * @see ScoreDefinition
 * @see HardAndSoftScoreDefinition
 * @author Geoffrey De Smet
 */
public abstract class AbstractScoreDefinition<S extends Score> implements ScoreDefinition<S>, Serializable {

    public S getPerfectMaximumScore() {
        // Hook which can be optionally overwritten by subclasses.
        return null;
    }

    public S getPerfectMinimumScore() {
        // Hook which can be optionally overwritten by subclasses
        return null;
    }

}
