package org.drools.solver.core.localsearch.decider;

import java.util.Random;

import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class MoveScope {

    private final StepScope stepScope;
    private Move move = null;
    private Move undoMove = null;
    private double acceptChance = Double.NaN;
    private double score = Double.NEGATIVE_INFINITY;
    private double decisionScore = Double.NaN;

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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(double decisionScore) {
        this.decisionScore = decisionScore;
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
