package org.drools.solver.core.score.constraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Geoffrey De Smet
 */
public class ConstraintOccurrence {

    private String ruleId;
    private Object[] objects;

    public ConstraintOccurrence(String ruleId, Object ... objects) {
        this.ruleId = ruleId;
        this.objects = objects;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ConstraintOccurrence) {
            ConstraintOccurrence other = (ConstraintOccurrence) o;
            return new EqualsBuilder()
                    .append(ruleId, other.ruleId)
                    .append(objects, other.objects)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(ruleId)
                .append(objects)
                .toHashCode();
    }

}
