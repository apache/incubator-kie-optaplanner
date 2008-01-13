package org.drools.solver.core.score.constraint;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Geoffrey De Smet
 */
public abstract class ConstraintOccurrence {

    protected String ruleId;
    protected ConstraintType constraintType;
    protected Object[] causes;

    public ConstraintOccurrence(String ruleId, Object... causes) {
        this(ruleId, ConstraintType.NEGATIVE_HARD, causes);
    }

    public ConstraintOccurrence(String ruleId, ConstraintType constraintType, Object... causes) {
        this.ruleId = ruleId;
        this.constraintType = constraintType;
        this.causes = causes;
    }

    public String getRuleId() {
        return ruleId;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public Object[] getCauses() {
        return causes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ConstraintOccurrence) {
            ConstraintOccurrence other = (ConstraintOccurrence) o;
            return new EqualsBuilder()
                    .append(ruleId, other.ruleId)
                    .append(constraintType, other.constraintType)
                    .append(causes, other.causes)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(ruleId)
                .append(constraintType)
                .append(causes)
                .toHashCode();
    }

    public String toString() {
        return ruleId + "/" + constraintType + ":" + Arrays.toString(causes);
    }

}
