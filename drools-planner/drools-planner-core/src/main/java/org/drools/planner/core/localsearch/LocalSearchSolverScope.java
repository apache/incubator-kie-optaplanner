package org.drools.planner.core.localsearch;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.drools.ClassObjectFilter;
import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.planner.core.score.calculator.ScoreCalculator;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.DoubleConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class LocalSearchSolverScope {

    public static final String GLOBAL_SCORE_CALCULATOR_KEY = "scoreCalculator";

    protected RuleBase ruleBase;
    private ScoreDefinition scoreDefinition;

    private long startingSystemTimeMillis;

    private Solution workingSolution;
    private StatefulSession workingMemory;
    private ScoreCalculator workingScoreCalculator;
    private Random workingRandom;

    private Score startingScore;

    private int bestSolutionStepIndex;
    private Solution bestSolution;
    private Score bestScore; // TODO remove me

    private StepScope lastCompletedStepScope;

    public RuleBase getRuleBase() {
        return ruleBase;
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public ScoreDefinition getScoreDefinition() {
        return scoreDefinition;
    }

    public void setScoreDefinition(ScoreDefinition scoreDefinition) {
        this.scoreDefinition = scoreDefinition;
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

    public Score getStartingScore() {
        return startingScore;
    }

    public void setStartingScore(Score startingScore) {
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

    public Score getBestScore() {
        return bestScore;
    }

    public void setBestScore(Score bestScore) {
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

    public Score calculateScoreFromWorkingMemory() {
        workingMemory.fireAllRules();
        Score score = workingScoreCalculator.calculateScore();
        workingSolution.setScore(score);
        return score;
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

    /**
     * TODO Refactor this with the ConstraintOccurrenceTotal class: https://jira.jboss.org/jira/browse/JBRULES-2510
     * @return never null
     */
    public String buildConstraintOccurrenceSummary() {
        if (workingMemory == null) {
            return "  The workingMemory is null.";
        }
        Map<String, Number> scoreTotalMap = new TreeMap<String, Number>();
        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
                new ClassObjectFilter(ConstraintOccurrence.class));
        while (it.hasNext()) {
            ConstraintOccurrence occurrence = it.next();
            Number scoreTotalNumber = scoreTotalMap.get(occurrence.getRuleId());
            if (occurrence instanceof IntConstraintOccurrence) {
                int scoreTotal = scoreTotalNumber == null ? 0 : (Integer) scoreTotalNumber;
                scoreTotal += ((IntConstraintOccurrence) occurrence).getWeight();
                scoreTotalMap.put(occurrence.getRuleId(), scoreTotal);
            } else if (occurrence instanceof DoubleConstraintOccurrence) {
                double scoreTotal = scoreTotalNumber == null ? 0 : (Double) scoreTotalNumber;
                scoreTotal += ((DoubleConstraintOccurrence) occurrence).getWeight();
                scoreTotalMap.put(occurrence.getRuleId(), scoreTotal);
            } else if (occurrence instanceof UnweightedConstraintOccurrence) {
                int scoreTotal = scoreTotalNumber == null ? 0 : (Integer) scoreTotalNumber;
                scoreTotal += 1;
                scoreTotalMap.put(occurrence.getRuleId(), scoreTotal);
            } else {
                throw new IllegalStateException("Cannot determine occurrenceScore of ConstraintOccurrence class: "
                        + occurrence.getClass());
            }
        }
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, Number> scoreTotalEntry : scoreTotalMap.entrySet()) {
            summary.append("  Score rule (").append(scoreTotalEntry.getKey()).append(") has score total (")
                    .append(scoreTotalEntry.getValue()).append(").\n");
        }
        return summary.toString();
    }

}
