package org.drools.solver.examples.common.solver.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Geoffrey De Smet
 */
public class WorkaroundMultiplePatternAccumulate {

    private String ruleId;
    private Object[] objects;

    public WorkaroundMultiplePatternAccumulate(String ruleId, Object ... objects) {
        this.ruleId = ruleId;
        this.objects = objects;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof WorkaroundMultiplePatternAccumulate) {
            WorkaroundMultiplePatternAccumulate other = (WorkaroundMultiplePatternAccumulate) o;
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
