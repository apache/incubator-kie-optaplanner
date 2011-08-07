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

package org.drools.planner.core.localsearch.decider.deciderscorecomparator;

import org.drools.planner.core.localsearch.LocalSearchSolverPhaseScope;
import org.drools.planner.core.localsearch.LocalSearchStepScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for {@link DeciderScoreComparatorFactory}.
 * @see DeciderScoreComparatorFactory
 */
public abstract class AbstractDeciderScoreComparatorFactory implements DeciderScoreComparatorFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void phaseStarted(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void beforeDeciding(LocalSearchStepScope localSearchStepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepDecided(LocalSearchStepScope localSearchStepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void stepTaken(LocalSearchStepScope localSearchStepScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

    public void phaseEnded(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
        // Hook which can be optionally overwritten by subclasses.
    }

}
