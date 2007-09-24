package org.drools.solver.core.localsearch.finish;

/**
 * @author Geoffrey De Smet
 */
public class FeasableScoreFinish extends AbstractFinish {

    private double feasableScore;

    private double startingScore;
    private double totalDelta;
    
    public void setFeasableScore(double feasableScore) {
        this.feasableScore = feasableScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted() {
        startingScore = localSearchSolver.getStepScore();
        totalDelta = startingScore - feasableScore;
    }

    public boolean isFinished() {
        double bestScore = localSearchSolver.getBestScore();
        return bestScore >= feasableScore;
    }

    public double calculateTimeGradient() {
        double stepScore = localSearchSolver.getStepScore();
        double stepDelta = startingScore - stepScore;
        double timeGradient = stepDelta / totalDelta;
        return Math.min(timeGradient, 1.0);
    }
    
}
