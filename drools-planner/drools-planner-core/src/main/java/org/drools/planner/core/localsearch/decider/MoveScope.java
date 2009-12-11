package org.drools.planner.core.localsearch.decider;

import java.util.Random;

import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.DefaultHardAndSoftScore;

/**
 * @author Geoffrey De Smet
 */
public class MoveScope {

    private final StepScope stepScope;
    private Move move = null;
    private Move undoMove = null;
    private double acceptChance = Double.NaN;
    private Score score = DefaultHardAndSoftScore.valueOf(Integer.MIN_VALUE, Integer.MIN_VALUE);

    public MoveScope(StepScope stepScope) {
        this.stepScope = stepScope;
    }

    public StepScope getStepScope() {
        return stepScope;
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

    public double getAcceptChance() {
        return acceptChance;
    }

    public void setAcceptChance(double acceptChance) {
        this.acceptChance = acceptChance;
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

    public Solution getWorkingSolution() {
        return stepScope.getWorkingSolution();
    }

    public WorkingMemory getWorkingMemory() {
        return stepScope.getWorkingMemory();
    }

    public Random getWorkingRandom() {
        return stepScope.getWorkingRandom();
    }

}
