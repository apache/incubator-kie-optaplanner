package org.drools.planner.core.solution.initializer;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractStartingSolutionInitializer implements StartingSolutionInitializer {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isSolutionInitialized(LocalSearchSolverScope localSearchSolverScope) {
        return false;
    }

}