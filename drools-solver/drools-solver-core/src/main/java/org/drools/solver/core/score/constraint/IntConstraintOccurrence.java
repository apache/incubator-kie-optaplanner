package org.drools.solver.core.score.constraint;

/**
 * @author Geoffrey De Smet
 */
public class IntConstraintOccurrence extends ConstraintOccurrence {

    protected int weight;

    public IntConstraintOccurrence(String ruleId, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, causes);
    }

    public IntConstraintOccurrence(String ruleId, ConstraintType constraintType, Object... causes) {
        this(ruleId, constraintType, 1, causes);
    }

    public IntConstraintOccurrence(String ruleId, int weight, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, weight, causes);
    }

    public IntConstraintOccurrence(String ruleId, ConstraintType constraintType, int weight, Object... causes) {
        super(ruleId, constraintType, causes);
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

}
