/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.solver.termination;

import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.solver.thread.ChildThreadType;

public class ChildThreadPlumbingTermination<Solution_> extends AbstractTermination<Solution_> {

    protected boolean terminateChildren = false;

    // ************************************************************************
    // Plumbing worker methods
    // ************************************************************************

    /**
     * This method is thread-safe.
     *
     * @return true if termination hasn't been requested previously
     */
    public synchronized boolean terminateChildren() {
        boolean terminationEarlySuccessful = !terminateChildren;
        terminateChildren = true;
        return terminationEarlySuccessful;
    }

    // ************************************************************************
    // Termination worker methods
    // ************************************************************************

    @Override
    public synchronized boolean isSolverTerminated(SolverScope<Solution_> solverScope) {
        // Destroying a thread pool with solver threads will only cause it to interrupt those child solver threads
        if (Thread.currentThread().isInterrupted()) { // Does not clear the interrupted flag
            logger.info("A child solver thread got interrupted, so these child solvers are terminating early.");
            terminateChildren = true;
        }
        return terminateChildren;
    }

    @Override
    public boolean isPhaseTerminated(AbstractPhaseScope<Solution_> phaseScope) {
        throw new IllegalStateException(ChildThreadPlumbingTermination.class.getSimpleName()
                + " configured only as solver termination."
                + " It is always bridged to phase termination.");
    }

    @Override
    public double calculateSolverTimeGradient(SolverScope<Solution_> solverScope) {
        return -1.0; // Not supported
    }

    @Override
    public double calculatePhaseTimeGradient(AbstractPhaseScope<Solution_> phaseScope) {
        throw new IllegalStateException(ChildThreadPlumbingTermination.class.getSimpleName()
                + " configured only as solver termination."
                + " It is always bridged to phase termination.");
    }

    // ************************************************************************
    // Other methods
    // ************************************************************************

    @Override
    public Termination<Solution_> createChildThreadTermination(SolverScope<Solution_> solverScope,
            ChildThreadType childThreadType) {
        return this;
    }

    @Override
    public String toString() {
        return "ChildThreadPlumbing()";
    }

}
