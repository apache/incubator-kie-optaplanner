/**
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.planner.core.localsearch.DefaultLocalSearchSolver;
import org.drools.planner.core.event.SolverEventSupport;
import org.drools.planner.core.event.SolverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link Solver}.
 * @see Solver
 * @see DefaultLocalSearchSolver
 * @author Geoffrey De Smet
 */
public abstract class AbstractSolver implements Solver {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected SolverEventSupport solverEventSupport = new SolverEventSupport(this);

    // TODO atomic enum with values NOT_STARTED, RUNNING, DONE, CANCELLED
    // TODO introduce a solver factory and make a solver one time use
    protected final AtomicBoolean terminatedEarly = new AtomicBoolean(false);

    public boolean terminateEarly() {
        boolean terminationEarlySuccessful = !terminatedEarly.getAndSet(true);
        if (terminationEarlySuccessful) {
            logger.info("Terminating solver early.");
        }
        return terminationEarlySuccessful;
    }

    public boolean isTerminatedEarly() {
        return terminatedEarly.get();
    }

    public final void solve() {
        terminatedEarly.set(false);
        solveImplementation();
    }

    protected abstract void solveImplementation();

    public void addEventListener(SolverEventListener eventListener) {
        solverEventSupport.addEventListener(eventListener);
    }

    public void removeEventListener(SolverEventListener eventListener) {
        solverEventSupport.removeEventListener(eventListener);
    }

}
