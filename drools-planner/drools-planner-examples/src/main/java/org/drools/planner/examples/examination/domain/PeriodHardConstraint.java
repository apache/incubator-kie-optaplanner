package org.drools.planner.examples.examination.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("PeriodHardConstraint")
public class PeriodHardConstraint extends AbstractPersistable implements Comparable<PeriodHardConstraint> {

    private PeriodHardConstraintType periodHardConstraintType;
    private Topic leftSideTopic;
    private Topic rightSideTopic;

    public PeriodHardConstraintType getPeriodHardConstraintType() {
        return periodHardConstraintType;
    }

    public void setPeriodHardConstraintType(PeriodHardConstraintType periodHardConstraintType) {
        this.periodHardConstraintType = periodHardConstraintType;
    }

    public Topic getLeftSideTopic() {
        return leftSideTopic;
    }

    public void setLeftSideTopic(Topic leftSideTopic) {
        this.leftSideTopic = leftSideTopic;
    }
    public Topic getRightSideTopic() {
        return rightSideTopic;
    }

    public void setRightSideTopic(Topic rightSideTopic) {
        this.rightSideTopic = rightSideTopic;
    }
    

    public int compareTo(PeriodHardConstraint other) {
        return new CompareToBuilder()
                .append(periodHardConstraintType, other.periodHardConstraintType)
                .append(leftSideTopic, other.leftSideTopic)
                .append(rightSideTopic, other.rightSideTopic)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return periodHardConstraintType + "@" + leftSideTopic.getId() + "&" + rightSideTopic.getId();
    }
    
}
