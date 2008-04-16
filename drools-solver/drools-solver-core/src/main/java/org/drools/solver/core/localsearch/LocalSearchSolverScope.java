package org.drools.solver.core.localsearch;

import java.util.Random;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.solver.core.score.calculator.ScoreCalculator;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class LocalSearchSolverScope {

    public static final String GLOBAL_SCORE_CALCULATOR_KEY = "scoreCalculator";

    protected RuleBase ruleBase;

    private long startingSystemTimeMillis;

    private Solution workingSolution;
    private StatefulSession workingMemory;
    private ScoreCalculator workingScoreCalculator;
    private Random workingRandom;

    private double startingScore;

    private int bestSolutionStepIndex;
    private Solution bestSolution;
    private double bestScore;

    private StepScope lastCompletedStepScope;

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public long getStartingSystemTimeMillis() {
        return startingSystemTimeMillis;
    }

    public void setStartingSystemTimeMillis(long startingSystemTimeMillis) {
        this.startingSystemTimeMillis = startingSystemTimeMillis;
    }

    public Solution getWorkingSolution() {
        return workingSolution;
    }

    public void setWorkingSolution(Solution workingSolution) {
        this.workingSolution = workingSolution;
        resetWorkingMemory();
    }

    public WorkingMemory getWorkingMemory() {
        return workingMemory;
    }

    public ScoreCalculator getWorkingScoreCalculator() {
        return workingScoreCalculator;
    }

    public void setWorkingScoreCalculator(ScoreCalculator workingScoreCalculator) {
        this.workingScoreCalculator = workingScoreCalculator;
    }

    public Random getWorkingRandom() {
        return workingRandom;
    }

    public void setWorkingRandom(Random workingRandom) {
        this.workingRandom = workingRandom;
    }

    public double getStartingScore() {
        return startingScore;
    }

    public void setStartingScore(double startingScore) {
        this.startingScore = startingScore;
    }

    public int getBestSolutionStepIndex() {
        return bestSolutionStepIndex;
    }

    public void setBestSolutionStepIndex(int bestSolutionStepIndex) {
        this.bestSolutionStepIndex = bestSolutionStepIndex;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Solution bestSolution) {
        this.bestSolution = bestSolution;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public StepScope getLastCompletedStepScope() {
        return lastCompletedStepScope;
    }

    public void setLastCompletedStepScope(StepScope lastCompletedStepScope) {
        this.lastCompletedStepScope = lastCompletedStepScope;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public double calculateScoreFromWorkingMemory() {
        workingMemory.fireAllRules();
        return workingScoreCalculator.calculateStepScore();
    }

    public void resetTimeMillisSpend() {
        startingSystemTimeMillis = System.currentTimeMillis();
    }

    public long calculateTimeMillisSpend() {
        long now = System.currentTimeMillis();
        return now - startingSystemTimeMillis;
    }

    private void resetWorkingMemory() {
        if (workingMemory != null) {
            workingMemory.dispose();
        }
        workingMemory = ruleBase.newStatefulSession();
        workingMemory.setGlobal(GLOBAL_SCORE_CALCULATOR_KEY, workingScoreCalculator);
//        if (logger.isTraceEnabled()) {
//            statefulSession.addEventListener(new DebugWorkingMemoryEventListener());
//        }
        for (Object fact : workingSolution.getFacts()) {
            workingMemory.insert(fact);
        }
    }

}
