package org.drools.solver.core.evaluation;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.solver.core.score.calculator.ScoreCalculator;
import org.drools.solver.core.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds a solution and its WorkingMemory.
 * Creates a WorkingMemory for a solution using the RuleBase.
 * @author Geoffrey De Smet
 */
public class EvaluationHandler {

    public static final String GLOBAL_SCORE_CALCULATOR_KEY = "scoreCalculator";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected RuleBase ruleBase;

    protected ScoreCalculator scoreCalculator;

    protected Solution solution;
    protected StatefulSession statefulSession;

    public RuleBase getRuleBase() {
        return ruleBase;
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public ScoreCalculator getScoreCalculator() {
        return scoreCalculator;
    }

    public void setScoreCalculator(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        resetStatefullSession();
    }

    public StatefulSession getStatefulSession() {
        return statefulSession;
    }

    /**
     * Resets the WorkingMemory by creating a new one and asserting the solution into it.
     */
    public void resetStatefullSession() {
        if (statefulSession != null) {
            statefulSession.dispose();
        }
        statefulSession = ruleBase.newStatefulSession();
        statefulSession.setGlobal(GLOBAL_SCORE_CALCULATOR_KEY, scoreCalculator);
//        if (logger.isTraceEnabled()) {
//            statefulSession.addEventListener(new DebugWorkingMemoryEventListener());
//        }
        for (Object fact : solution.getFacts()) {
            statefulSession.insert(fact);
        }
    }

    /**
     * Calculates the step score with the scoreCalculator.
     * This method makes sure that the WorkingMemory is up to date by firing all rules first.
     * @return the step score from the scoreCalculator
     */
    public double fireAllRulesAndCalculateStepScore() {
        statefulSession.fireAllRules();
        return scoreCalculator.calculateStepScore();
    }

    /**
     * Calculates the decision score with the scoreCalculator.
     * This method makes sure that the WorkingMemory is up to date by firing all rules first.
     * @return the decision score from the scoreCalculator
     */
    public double fireAllRulesAndCalculateDecisionScore() {
        statefulSession.fireAllRules();
        return scoreCalculator.calculateDecisionScore();
    }

}
