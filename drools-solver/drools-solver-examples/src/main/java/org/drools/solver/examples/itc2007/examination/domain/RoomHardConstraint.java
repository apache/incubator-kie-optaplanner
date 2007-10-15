package org.drools.solver.examples.itc2007.examination.domain;

import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class RoomHardConstraint extends AbstractPersistable implements Comparable<RoomHardConstraint> {

    private Exam exam;
    private RoomHardConstraintType roomHardConstraintType;

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public RoomHardConstraintType getRoomHardConstraintType() {
        return roomHardConstraintType;
    }

    public void setRoomHardConstraintType(RoomHardConstraintType roomHardConstraintType) {
        this.roomHardConstraintType = roomHardConstraintType;
    }

    public int compareTo(RoomHardConstraint other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    public RoomHardConstraint clone(Map<Long, Exam> idToClonedExamMap) {
        RoomHardConstraint clone = new RoomHardConstraint();
        clone.id = id;
        clone.exam = idToClonedExamMap.get(exam.getId());
        clone.roomHardConstraintType = roomHardConstraintType;
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + " {" + exam.getId() + " " + roomHardConstraintType + "}";
    }

}
