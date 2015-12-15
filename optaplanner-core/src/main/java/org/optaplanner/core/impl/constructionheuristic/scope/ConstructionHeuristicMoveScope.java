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

package org.optaplanner.core.impl.constructionheuristic.scope;

import java.util.Random;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class ConstructionHeuristicMoveScope {

    private final ConstructionHeuristicStepScope stepScope;

    private int moveIndex;
    private Move move = null;
    private Move undoMove = null;
    private Score score = null;

    public ConstructionHeuristicMoveScope(ConstructionHeuristicStepScope stepScope) {
        this.stepScope = stepScope;
    }

    public ConstructionHeuristicStepScope getStepScope() {
        return stepScope;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public void setMoveIndex(int moveIndex) {
        this.moveIndex = moveIndex;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Move getUndoMove() {
        return undoMove;
    }

    public void setUndoMove(Move undoMove) {
        this.undoMove = undoMove;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public ScoreDirector getScoreDirector() {
        return stepScope.getScoreDirector();
    }

    public Solution getWorkingSolution() {
        return stepScope.getWorkingSolution();
    }

    public Random getWorkingRandom() {
        return stepScope.getWorkingRandom();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + stepScope.getStepIndex() + "/" + moveIndex + ")";
    }

}
