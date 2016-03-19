/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      hhttp://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.phase.scope;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public abstract class AbstractPhaseScope<Solution_> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final DefaultSolverScope<Solution_> solverScope;

    protected long startingSystemTimeMillis;

    protected Score startingScore;

    protected int bestSolutionStepIndex;

    public AbstractPhaseScope(DefaultSolverScope<Solution_> solverScope) {
        this.solverScope = solverScope;
    }

    public DefaultSolverScope<Solution_> getSolverScope() {
        return solverScope;
    }

    public long getStartingSystemTimeMillis() {
        return startingSystemTimeMillis;
    }

    public Score getStartingScore() {
        return startingScore;
    }

    public int getBestSolutionStepIndex() {
        return bestSolutionStepIndex;
    }

    public void setBestSolutionStepIndex(int bestSolutionStepIndex) {
        this.bestSolutionStepIndex = bestSolutionStepIndex;
    }

    public abstract AbstractStepScope<Solution_> getLastCompletedStepScope();

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public void reset() {
        startingSystemTimeMillis = System.currentTimeMillis();
        bestSolutionStepIndex = -1;
        // TODO Usage of solverScope.getBestScore() would be better performance wise but is null with a uninitialized score
        startingScore = solverScope.calculateScore();
        if (getLastCompletedStepScope().getStepIndex() < 0) {
            getLastCompletedStepScope().setScore(startingScore);
        }
    }

    public SolutionDescriptor<Solution_> getSolutionDescriptor() {
        return solverScope.getSolutionDescriptor();
    }

    public ScoreDefinition getScoreDefinition() {
        return solverScope.getScoreDefinition();
    }

    public long calculateSolverTimeMillisSpent() {
        return solverScope.calculateTimeMillisSpent();
    }

    public long calculatePhaseTimeMillisSpent() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

    public InnerScoreDirector<Solution_> getScoreDirector() {
        return solverScope.getScoreDirector();
    }

    public Solution_ getWorkingSolution() {
        return solverScope.getWorkingSolution();
    }

    public int getWorkingEntityCount() {
        return solverScope.getWorkingEntityCount();
    }

    public List<Object> getWorkingEntityList() {
        return solverScope.getWorkingEntityList();
    }

    public int getWorkingValueCount() {
        return solverScope.getWorkingValueCount();
    }

    public Score calculateScore() {
        return solverScope.calculateScore();
    }

    public void assertExpectedWorkingScore(Score expectedWorkingScore, Object completedAction) {
        solverScope.assertExpectedWorkingScore(expectedWorkingScore, completedAction);
    }

    public void assertWorkingScoreFromScratch(Score workingScore, Object completedAction) {
        solverScope.assertWorkingScoreFromScratch(workingScore, completedAction);
    }

    public void assertExpectedUndoMoveScore(Move move, Move undoMove, Score beforeMoveScore) {
        Score undoScore = calculateScore();
        if (!undoScore.equals(beforeMoveScore)) {
            logger.trace("        Corruption detected. Diagnosing...");
            // TODO PLANNER-421 Avoid undoMove.toString() because it's stale (because the move is already done)
            String undoMoveString = "Undo(" + move + ")";
            // Precondition: assert that are probably no corrupted score rules.
            getScoreDirector().assertWorkingScoreFromScratch(undoScore, undoMoveString);
            // Precondition: assert that shadow variable after the undoMove aren't stale
            getScoreDirector().assertShadowVariablesAreNotStale(undoScore, undoMoveString);
            throw new IllegalStateException("UndoMove corruption: the beforeMoveScore (" + beforeMoveScore
                    + ") is not the undoScore (" + undoScore
                    + ") which is the uncorruptedScore (" + undoScore + ") of the workingSolution.\n"
                    + "  1) Enable EnvironmentMode " + EnvironmentMode.FULL_ASSERT
                    + " (if you haven't already) to fail-faster in case there's a score corruption.\n"
                    + "  2) Check the Move.createUndoMove(...) method of the moveClass (" + move.getClass() + ")."
                    + " The move (" + move + ") might have a corrupted undoMove (" + undoMoveString + ").\n"
                    + "  3) Check your custom " + VariableListener.class.getSimpleName() + "s (if you have any)"
                    + " for shadow variables that are used by the score constraints with a different score weight"
                    + " between the beforeMoveScore (" + beforeMoveScore + ") and the undoScore (" + undoScore + ").");
        }
    }

    public Random getWorkingRandom() {
        return solverScope.getWorkingRandom();
    }

    public boolean isBestSolutionInitialized() {
        return solverScope.isBestSolutionInitialized();
    }

    public Score getBestScore() {
        return solverScope.getBestScore();
    }

    public String getBestScoreWithUninitializedPrefix() {
        return solverScope.getBestScoreWithUninitializedPrefix();
    }

    public long getPhaseBestSolutionTimeMillis() {
        long bestSolutionTimeMillis = solverScope.getBestSolutionTimeMillis();
        // If the termination is explicitly phase configured, previous phases must not affect it
        if (bestSolutionTimeMillis < startingSystemTimeMillis) {
            bestSolutionTimeMillis = startingSystemTimeMillis;
        }
        return bestSolutionTimeMillis;
    }

    public int getNextStepIndex() {
        return getLastCompletedStepScope().getStepIndex() + 1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName(); // TODO add + "(" + phaseIndex + ")"
    }

}
