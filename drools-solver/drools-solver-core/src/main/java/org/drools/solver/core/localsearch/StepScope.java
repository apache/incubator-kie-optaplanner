package org.drools.solver.core.localsearch;

import java.util.Random;
import java.util.Comparator;

import org.drools.WorkingMemory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class StepScope {

    private final LocalSearchSolverScope localSearchSolverScope;

    private int stepIndex = -1;
    private double timeGradient = Double.NaN;
    private Comparator<Score> deciderScoreComparator;
    private Move step = null;
    private Move undoStep = null;
    private Score score = null;
    // Stays null if there is no need to clone it
    private Solution clonedSolution = null;

    public StepScope(LocalSearchSolverScope localSearchSolverScope) {
        this.localSearchSolverScope = localSearchSolverScope;
    }

    public LocalSearchSolverScope getLocalSearchSolverScope() {
        return localSearchSolverScope;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public double getTimeGradient() {
        return timeGradient;
    }

    public void setTimeGradient(double timeGradient) {
        this.timeGradient = timeGradient;
    }

    public Comparator<Score> getDeciderScoreComparator() {
        return deciderScoreComparator;
    }

    public void setDeciderScoreComparator(Comparator<Score> deciderScoreComparator) {
        this.deciderScoreComparator = deciderScoreComparator;
    }

    public Move getStep() {
        return step;
    }

    public void setStep(Move step) {
        this.step = step;
    }

    public Move getUndoStep() {
        return undoStep;
    }

    public void setUndoStep(Move undoStep) {
        this.undoStep = undoStep;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Solution getClonedSolution() {
        return clonedSolution;
    }

    public void setClonedSolution(Solution clonedSolution) {
        this.clonedSolution = clonedSolution;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public Solution getWorkingSolution() {
        return localSearchSolverScope.getWorkingSolution();
    }

    public WorkingMemory getWorkingMemory() {
        return localSearchSolverScope.getWorkingMemory();
    }

    public Random getWorkingRandom() {
        return localSearchSolverScope.getWorkingRandom();
    }

    public Solution createOrGetClonedSolution() {
        if (clonedSolution == null) {
            clonedSolution = getWorkingSolution().cloneSolution();
        }
        return clonedSolution;
    }

}
