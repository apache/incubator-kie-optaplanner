/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.localsearch.decider.acceptor.tabu;

import java.util.Collection;
import java.util.Collections;

import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

@Deprecated
public class SolutionTabuAcceptor extends AbstractTabuAcceptor {

    public SolutionTabuAcceptor(String logIndentation) {
        super(logIndentation);
        // Disable aspiration by default because it's useless on solution tabu
        aspirationEnabled = false;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    protected Collection<? extends Object> findTabu(LocalSearchMoveScope moveScope) {
        return Collections.singletonList(moveScope.getWorkingSolution());
    }

    @Override
    protected Collection<? extends Object> findNewTabu(LocalSearchStepScope stepScope) {
        // TODO this should be better done in stepEnded
        return Collections.singletonList(stepScope.createOrGetClonedSolution());
    }

    @Override
    public void phaseStarted(LocalSearchPhaseScope phaseScope) {
        super.phaseStarted(phaseScope);
        // Add the starting solution to the tabu list
        Object tabu = phaseScope.getScoreDirector().cloneWorkingSolution();
        tabuToStepIndexMap.put(tabu, 0); // TODO should -1 when AbstractTabuAcceptor can handle that
        tabuSequenceDeque.add(tabu);
    }

}
