package org.drools.solver.core.localsearch;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.localsearch.bestsolution.BestSolutionRecaller;
import org.drools.solver.core.localsearch.decider.Decider;
import org.drools.solver.core.localsearch.finish.Finish;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class DefaultLocalSearchSolver implements LocalSearchSolver, LocalSearchSolverLifecycleListener {

    protected final transient Log log = LogFactory.getLog(getClass());

    protected long randomSeed; // TODO refactor to AbstractSolver

    protected EvaluationHandler evaluationHandler; // TODO refactor to AbstractSolver
    protected BestSolutionRecaller bestSolutionRecaller;
    protected Finish finish;
    protected Decider decider;

    protected long startingSystemTimeMillis;
    protected Random random;
    protected int stepIndex;
    protected double stepScore;

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public EvaluationHandler getEvaluationHandler() {
        return evaluationHandler;
    }

    public void setEvaluationHandler(EvaluationHandler evaluationHandler) {
        this.evaluationHandler = evaluationHandler;
    }

    public void setBestSolutionRecaller(BestSolutionRecaller bestSolutionRecaller) {
        this.bestSolutionRecaller = bestSolutionRecaller;
        bestSolutionRecaller.setLocalSearchSolver(this);
    }

    public void setDecider(Decider decider) {
        this.decider = decider;
        decider.setLocalSearchSolver(this);
    }

    public void setFinish(Finish finish) {
        this.finish = finish;
        finish.setLocalSearchSolver(this);
    }


    public long getTimeMillisSpend() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

    public Random getRandom() {
        return random;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public double getStepScore() {
        return stepScore;
    }

    public void setStartingSolution(Solution solution) {
        evaluationHandler.setSolution(solution);
    }

    public Solution getCurrentSolution() {
        return evaluationHandler.getSolution();
    }

    public int getBestSolutionStepIndex() {
        return bestSolutionRecaller.getBestSolutionStepIndex();
    }

    public Solution getBestSolution() {
        return bestSolutionRecaller.getBestSolution();
    }

    public double getBestScore() {
        return bestSolutionRecaller.getBestScore();
    }

    public double calculateTimeGradient() {
        return finish.calculateTimeGradient();
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solve() {
        solvingStarted();
        while (!finish.isFinished()) {
            beforeDeciding();
            Move nextStep = decider.decideNextStep();
            if (nextStep == null) {
                log.warn("No move accepted for step (" + getStepIndex() + "). Finishing early.");
                break;
            }
            if (log.isInfoEnabled()) {
                log.info("Step (" + getStepIndex() + "), time spend (" + getTimeMillisSpend() + ") doing next step (" + nextStep + ").");
            }
            stepDecided(nextStep);
            nextStep.doMove(evaluationHandler.getStatefulSession());
            stepScore = evaluationHandler.fireAllRulesAndCalculateStepScore();
            stepIndex++;
            stepTaken();
        }
        solvingEnded();
    }

    public void solvingStarted() {
        log.info("Solving with random seed (" + randomSeed + ").");
        startingSystemTimeMillis = System.currentTimeMillis();
        random = new Random(randomSeed);
        stepIndex = 0;
        stepScore = evaluationHandler.fireAllRulesAndCalculateStepScore();
        bestSolutionRecaller.solvingStarted();
        finish.solvingStarted();
        decider.solvingStarted();
    }

    public void beforeDeciding() {
        bestSolutionRecaller.beforeDeciding();
        finish.beforeDeciding();
        decider.beforeDeciding();
    }

    public void stepDecided(Move step) {
        bestSolutionRecaller.stepDecided(step);
        finish.stepDecided(step);
        decider.stepDecided(step);
    }

    public void stepTaken() {
        bestSolutionRecaller.stepTaken();
        finish.stepTaken();
        decider.stepTaken();
    }

    public void solvingEnded() {
        bestSolutionRecaller.solvingEnded();
        finish.solvingEnded();
        decider.solvingEnded();
        evaluationHandler.setSolution(bestSolutionRecaller.getBestSolution());
        log.info("Solved in " + stepIndex + " steps and " + getTimeMillisSpend() + " time millis spend.");
    }

}
