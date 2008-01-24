package org.drools.solver.examples.itc2007.examination.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExamSwitchMove implements Move, TabuPropertyEnabled {

    private Exam leftExam;
    private Exam rightExam;

    public ExamSwitchMove(Exam leftExam, Exam rightExam) {
        this.leftExam = leftExam;
        this.rightExam = rightExam;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !(ObjectUtils.equals(leftExam.getPeriod(), rightExam.getPeriod())
                && ObjectUtils.equals(leftExam.getRoom(), rightExam.getRoom()));
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new ExamSwitchMove(rightExam, leftExam);
    }

    public void doMove(WorkingMemory workingMemory) {
        // TODO also move coincidence
        FactHandle leftExamHandle = workingMemory.getFactHandle(leftExam);
        FactHandle rightExamHandle = workingMemory.getFactHandle(rightExam);
        Period oldLeftPeriod = leftExam.getPeriod();
        Room oldLeftRoom = leftExam.getRoom();
        leftExam.setPeriod(rightExam.getPeriod());
        leftExam.setRoom(rightExam.getRoom());
        rightExam.setPeriod(oldLeftPeriod);
        rightExam.setRoom(oldLeftRoom);
        workingMemory.update(leftExamHandle, leftExam);
        workingMemory.update(rightExamHandle, rightExam);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<Exam>asList(leftExam, rightExam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ExamSwitchMove) {
            ExamSwitchMove other = (ExamSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftExam, other.leftExam)
                    .append(rightExam, other.rightExam)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftExam)
                .append(rightExam)
                .toHashCode();
    }

    public String toString() {
        return leftExam + " <=> " + rightExam;
    }

}