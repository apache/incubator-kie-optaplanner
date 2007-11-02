package org.drools.solver.examples.itc2007.examination.solver;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class RoomChangeMove implements Move, TabuPropertyEnabled {

    private Exam exam;
    private Room toRoom;

    public RoomChangeMove(Exam exam, Room toRoom) {
        this.exam = exam;
        this.toRoom = toRoom;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        Room fromRoom = exam.getRoom();
        if (fromRoom == null) {
            return (toRoom != null);
        }
        return !fromRoom.equals(toRoom);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new RoomChangeMove(exam, exam.getRoom());
    }

    public void doMove(WorkingMemory workingMemory) {
        FactHandle lessonHandle = workingMemory.getFactHandle(exam);
        exam.setRoom(toRoom);
        workingMemory.update(lessonHandle, exam);
    }

    public List<? extends Object> getTabuPropertyList() {
        return Collections.singletonList(exam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PeriodChangeMove) {
            RoomChangeMove other = (RoomChangeMove) o;
            return new EqualsBuilder()
                    .append(exam, other.exam)
                    .append(toRoom, other.toRoom)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(exam)
                .append(toRoom)
                .toHashCode();
    }

    public String toString() {
        return exam + " => " + toRoom;
    }

}