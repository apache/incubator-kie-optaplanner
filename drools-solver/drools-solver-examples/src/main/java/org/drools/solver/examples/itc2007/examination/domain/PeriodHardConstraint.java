package org.drools.solver.examples.itc2007.examination.domain;

import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class PeriodHardConstraint extends AbstractPersistable implements Comparable<PeriodHardConstraint> {

    private Exam leftSideExam;
    private PeriodHardConstraintType periodHardConstraintType;
    private Exam rightSideExam;

    public Exam getLeftSideExam() {
        return leftSideExam;
    }

    public void setLeftSideExam(Exam leftSideExam) {
        this.leftSideExam = leftSideExam;
    }

    public PeriodHardConstraintType getPeriodHardConstraintType() {
        return periodHardConstraintType;
    }

    public void setPeriodHardConstraintType(PeriodHardConstraintType periodHardConstraintType) {
        this.periodHardConstraintType = periodHardConstraintType;
    }

    public Exam getRightSideExam() {
        return rightSideExam;
    }

    public void setRightSideExam(Exam rightSideExam) {
        this.rightSideExam = rightSideExam;
    }

    public int compareTo(PeriodHardConstraint other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    public PeriodHardConstraint clone(Map<Long, Exam> idToClonedExamMap) {
        PeriodHardConstraint clone = new PeriodHardConstraint();
        clone.id = id;
        clone.leftSideExam = idToClonedExamMap.get(leftSideExam.getId());
        clone.periodHardConstraintType = periodHardConstraintType;
        clone.rightSideExam = idToClonedExamMap.get(rightSideExam.getId());
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + " {" + leftSideExam.getId() + " " + periodHardConstraintType + " " + rightSideExam.getId() + "}";
    }
    
}
