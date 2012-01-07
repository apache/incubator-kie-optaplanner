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

package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;
import java.util.Collections;

import org.drools.planner.core.localsearch.LocalSearchSolverPhaseScope;
import org.drools.planner.core.localsearch.LocalSearchStepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;

public class SolutionTabuAcceptor extends AbstractTabuAcceptor {

    public SolutionTabuAcceptor() {
        // Disable aspiration by default because it's useless on solution tabu
        aspirationEnabled = false;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(MoveScope moveScope) {
        return Collections.singletonList(moveScope.getWorkingSolution());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(LocalSearchStepScope localSearchStepScope) {
        // TODO this should be better done in stepTaken
        return Collections.singletonList(localSearchStepScope.createOrGetClonedSolution());
    }
    
    /**
     * This implementation will add the starting solution to the tabu list to
     * avoid visiting this solution (see JBRULES-3334).
     */
    @Override
    public void phaseStarted(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
    	super.phaseStarted(localSearchSolverPhaseScope);
    	// Get a clone of the current solution, then add it to the tabu list.
    	Object tabu = localSearchSolverPhaseScope.getWorkingSolution().cloneSolution();
        tabuToStepIndexMap.put(tabu, 0);
        tabuSequenceList.add(tabu);
    }

}
