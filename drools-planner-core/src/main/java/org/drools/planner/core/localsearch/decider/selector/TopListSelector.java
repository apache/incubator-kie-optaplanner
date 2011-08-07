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

package org.drools.planner.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverPhaseScope;
import org.drools.planner.core.localsearch.LocalSearchStepScope;
import org.drools.planner.core.move.Move;

public class TopListSelector extends AbstractSelector {

    private int topSize;

    private List<Move> topList;

    public void setTopSize(int topSize) {
        this.topSize = topSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
        topList = Collections.emptyList();
    }

    public Iterator<Move> moveIterator(LocalSearchStepScope localSearchStepScope) {
        return topList.iterator();
    }

    @Override
    public void stepTaken(LocalSearchStepScope localSearchStepScope) {
        topList = decider.getForager().getTopList(topSize);
    }

}
