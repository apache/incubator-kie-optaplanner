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

package org.drools.planner.core.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverPhaseScope;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.solution.Solution;

public abstract class CachedMoveFactory extends AbstractMoveFactory {

    protected List<Move> cachedMoveList;

    public List<Move> getCachedMoveList() {
        return cachedMoveList;
    }

    @Override
    public void phaseStarted(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
        cachedMoveList = createCachedMoveList(localSearchSolverPhaseScope.getWorkingSolution());
    }

    public abstract List<Move> createCachedMoveList(Solution solution);

    public List<Move> createMoveList(Solution solution) {
        // Shallow copy so it can be shuffled and filtered etc
        return new ArrayList<Move>(cachedMoveList);
    }

}
