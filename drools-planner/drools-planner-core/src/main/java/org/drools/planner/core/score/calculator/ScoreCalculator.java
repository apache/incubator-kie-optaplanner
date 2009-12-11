package org.drools.planner.core.score.calculator;

import org.drools.planner.core.score.Score;

/**
 * Evaluates a solution based on its WorkingMemory (which is part of the rule production system).
 * @TODO score-in-solution refactor
 * @author Geoffrey De Smet
 */
public interface ScoreCalculator {

    /**
     * Calculates the score: the solution (encountered at a step)
     * with the highest score will be seen as the the best solution.
     * </p>
     * The step score calculation should be kept stable over all steps.
     * </p>
     * When the solution is modified during a Move,
     * the WorkingMemory's FactHandles should have been correctly notified.
     * Before the score is calculated, all rules are fired,
     * which should trigger an update of this instance.
     * @return never null, the score of the solution
     */
    Score calculateScore();

}
