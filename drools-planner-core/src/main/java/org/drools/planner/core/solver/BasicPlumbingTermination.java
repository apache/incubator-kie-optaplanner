/*
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

package org.drools.planner.core.solver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.termination.AbstractTermination;

public class BasicPlumbingTermination extends AbstractTermination {

    protected AtomicBoolean terminatedEarly = new AtomicBoolean(false);
    protected BlockingQueue<PlanningFactChange> planningFactChangeQueue = new LinkedBlockingQueue<PlanningFactChange>();

    // ************************************************************************
    // Plumbing worker methods
    // ************************************************************************
    
    public void resetTerminateEarly() {
        terminatedEarly.set(false);
    }

    public boolean terminateEarly() {
        boolean terminationEarlySuccessful = !terminatedEarly.getAndSet(true);
        if (terminationEarlySuccessful) {
            logger.info("Terminating solver early.");
        }
        return terminationEarlySuccessful;
    }

    public boolean isTerminateEarly() {
        return terminatedEarly.get();
    }

    public boolean addPlanningFactChange(PlanningFactChange planningFactChange) {
        return planningFactChangeQueue.add(planningFactChange);
    }

    public BlockingQueue<PlanningFactChange> getPlanningFactChangeQueue() {
        return planningFactChangeQueue;
    }

    // ************************************************************************
    // Termination worker methods
    // ************************************************************************

    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        return terminatedEarly.get() || !planningFactChangeQueue.isEmpty();
    }

    public boolean isPhaseTerminated(AbstractSolverPhaseScope solverPhaseScope) {
        throw new UnsupportedOperationException("BasicPlumbingTermination can only be used for solver termination.");
    }

    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        return -1.0; // Not supported
    }

    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope solverPhaseScope) {
        throw new UnsupportedOperationException("BasicPlumbingTermination can only be used for solver termination.");
    }

}
