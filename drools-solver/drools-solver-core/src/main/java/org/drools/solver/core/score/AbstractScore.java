package org.drools.solver.core.score;

import java.io.Serializable;

/**
 * Abstract superclass for {@link Score}.
 * <p/>
 * Subclasses must be immutable.
 * @see Score
 * @see DefaultHardAndSoftScore
 * @author Geoffrey De Smet
 */
public abstract class AbstractScore<S extends Score>
        implements Score<S>, Serializable {

}
