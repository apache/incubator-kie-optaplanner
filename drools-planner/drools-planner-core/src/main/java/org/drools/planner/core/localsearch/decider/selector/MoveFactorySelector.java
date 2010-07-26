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

package org.drools.planner.core.localsearch.decider.selector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.Decider;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.MoveFactory;

/**
 * @author Geoffrey De Smet
 */
public class MoveFactorySelector extends AbstractSelector {

    protected MoveFactory moveFactory;

    protected boolean shuffle = true;

    public void setMoveFactory(MoveFactory moveFactory) {
        this.moveFactory = moveFactory;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    @Override
    public void setDecider(Decider decider) {
        super.setDecider(decider);
        moveFactory.setDecider(decider);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        moveFactory.solvingStarted(localSearchSolverScope);
    }

    @Override
    public void beforeDeciding(StepScope stepScope) {
        moveFactory.beforeDeciding(stepScope);
    }

    public Iterator<Move> moveIterator(StepScope stepScope) {
        return selectMoveList(stepScope).iterator();
    }

    public List<Move> selectMoveList(StepScope stepScope) {
        List<Move> moveList = moveFactory.createMoveList(stepScope.getWorkingSolution());
        if (shuffle) {
            Collections.shuffle(moveList, stepScope.getWorkingRandom());
        }
        return moveList;
    }

    @Override
    public void stepDecided(StepScope stepScope) {
        moveFactory.stepDecided(stepScope);
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        moveFactory.stepTaken(stepScope);
    }

    @Override
    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
        moveFactory.solvingEnded(localSearchSolverScope);
    }

}
