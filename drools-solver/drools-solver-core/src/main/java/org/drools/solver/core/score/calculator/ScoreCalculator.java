package org.drools.solver.core.score.calculator;

/**
 * Evaluates a solution based on its WorkingMemory (which is part of the rule production system).
 * @author Geoffrey De Smet
 */
public interface ScoreCalculator {

    /**
     * Calculates the step score: the solution (encountered at a step)
     * with the highest step score will be seen as the the best solution.
     * </p>
     * The step score calculation should be kept stable over all steps.
     * </p>
     * When the solution is modified during a Move,
     * the WorkingMemory's FactHandles should have been correctly notified.
     * Before the score is calculated, all rules are fired,
     * which should trigger an update of this instance.
     * @return the step score of the solution
     */
    double calculateStepScore();

    /**
     * Calculates the decision score: the move with the highest decision score
     * will be chosen as the next step (provided it is accepted by the accepter of course).
     * </p>
     * The decision score calculation should be kept stable in a single step decision,
     * but it can change between steps.
     * For example: the weight of hard constraints can be lowered (hightened) if few (many) are broken
     * to allow to pass through (avoid) hard constraint solution zones.
     * </p>
     * When the solution is modified during a Move,
     * the WorkingMemory's FactHandles should have been correctly notified.
     * Before the score is calculated, all rules are fired,
     * which should trigger an update of this instance.
     * @return the decision score of the solution, ussually the same as the step score
     * @see AbstractScoreCalculator
     */
    double calculateDecisionScore();
    
}
