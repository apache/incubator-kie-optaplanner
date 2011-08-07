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

package org.drools.planner.core.phase;

import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.planner.core.bestsolution.BestSolutionRecaller;
import org.drools.planner.core.localsearch.DefaultLocalSearchSolverPhase;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListener;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleSupport;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.solver.DefaultSolverScope;
import org.drools.planner.core.termination.Termination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see DefaultLocalSearchSolverPhase
 */
public abstract class AbstractSolverPhase implements SolverPhase, SolverPhaseLifecycleListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Termination termination;

    protected BestSolutionRecaller bestSolutionRecaller;

    protected SolverPhaseLifecycleSupport solverPhaseLifecycleSupport = new SolverPhaseLifecycleSupport();

    public void setTermination(Termination termination) {
        this.termination = termination;
    }

    public void setBestSolutionRecaller(BestSolutionRecaller bestSolutionRecaller) {
        this.bestSolutionRecaller = bestSolutionRecaller;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void phaseStarted(AbstractSolverPhaseScope solverPhaseScope) {
        solverPhaseScope.reset();
        termination.phaseStarted(solverPhaseScope);
        solverPhaseLifecycleSupport.firePhaseStarted(solverPhaseScope);
    }

    public void beforeDeciding(AbstractStepScope stepScope) {
        termination.beforeDeciding(stepScope);
        solverPhaseLifecycleSupport.fireBeforeDeciding(stepScope);
    }

    public void stepDecided(AbstractStepScope stepScope) {
        termination.stepDecided(stepScope);
        solverPhaseLifecycleSupport.fireStepDecided(stepScope);
    }

    public void stepTaken(AbstractStepScope stepScope) {
        bestSolutionRecaller.extractBestSolution(stepScope);
        termination.stepTaken(stepScope);
        solverPhaseLifecycleSupport.fireStepTaken(stepScope);
    }

    public void phaseEnded(AbstractSolverPhaseScope solverPhaseScope) {
        DefaultSolverScope solverScope = solverPhaseScope.getSolverScope();
        // At the end of the phase, the best solution should be in the working memory for the next phase
        // TODO WORKAROUND because the best solution might not be initialized yet (and than it's currently null)
        Solution bestSolution = solverScope.getBestSolution();
        if (bestSolution != null) {
            solverScope.getSolutionDirector().setWorkingSolution(bestSolution);
        }
        termination.phaseEnded(solverPhaseScope);
        solverPhaseLifecycleSupport.firePhaseEnded(solverPhaseScope);
    }

    public void addSolverPhaseLifecycleListener(SolverPhaseLifecycleListener lifecycleListener) {
        solverPhaseLifecycleSupport.addEventListener(lifecycleListener);
    }

    public void removeSolverPhaseLifecycleListener(SolverPhaseLifecycleListener lifecycleListener) {
        solverPhaseLifecycleSupport.removeEventListener(lifecycleListener);
    }

}
