package org.drools.solver.core.solution.initializer;

import org.drools.solver.core.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractStartingSolutionInitializer implements StartingSolutionInitializer {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Solver solver;

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

}