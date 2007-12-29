package org.drools.solver.core.score.constraint;

/**
 * @author Geoffrey De Smet
 */
public class UnweightedConstraintOccurrence extends ConstraintOccurrence {

    public UnweightedConstraintOccurrence(String ruleId, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, causes);
    }

    public UnweightedConstraintOccurrence(String ruleId, ConstraintType constraintType, Object... causes) {
        super(ruleId, constraintType, causes);
    }

}