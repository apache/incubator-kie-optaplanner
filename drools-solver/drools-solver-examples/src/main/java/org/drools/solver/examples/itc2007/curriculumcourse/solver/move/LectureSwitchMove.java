package org.drools.solver.lectureples.itc2007.curriculumcourse.solver.move;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.solver.core.localsearch.decider.accepter.tabu.TabuPropertyEnabled;
import org.drools.solver.core.move.Move;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Period;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Room;
import org.drools.solver.examples.itc2007.curriculumcourse.solver.move.CurriculumCourseMoveHelper;

/**
 * @author Geoffrey De Smet
 */
public class LectureSwitchMove implements Move, TabuPropertyEnabled {

    private Lecture leftLecture;
    private Lecture rightLecture;

    public LectureSwitchMove(Lecture leftLecture, Lecture rightLecture) {
        this.leftLecture = leftLecture;
        this.rightLecture = rightLecture;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !(ObjectUtils.equals(leftLecture.getPeriod(), rightLecture.getPeriod())
                && ObjectUtils.equals(leftLecture.getRoom(), rightLecture.getRoom()));
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new LectureSwitchMove(rightLecture, leftLecture);
    }

    public void doMove(WorkingMemory workingMemory) {
        Period oldLeftPeriod = leftLecture.getPeriod();
        Period oldRightPeriod = rightLecture.getPeriod();
        Room oldLeftRoom = leftLecture.getRoom();
        Room oldRightRoom = rightLecture.getRoom();
        if (oldLeftPeriod.equals(oldRightPeriod)) {
            CurriculumCourseMoveHelper.moveRoom(workingMemory, leftLecture, oldRightRoom);
            CurriculumCourseMoveHelper.moveRoom(workingMemory, rightLecture, oldLeftRoom);
        } else if (oldLeftRoom.equals(oldRightRoom)) {
            CurriculumCourseMoveHelper.movePeriod(workingMemory, leftLecture, oldRightPeriod);
            CurriculumCourseMoveHelper.movePeriod(workingMemory, rightLecture, oldLeftPeriod);
        } else {
            CurriculumCourseMoveHelper.moveLecture(workingMemory, leftLecture, oldRightPeriod, oldRightRoom);
            CurriculumCourseMoveHelper.moveLecture(workingMemory, rightLecture, oldLeftPeriod, oldLeftRoom);
        }
    }

    public Collection<? extends Object> getTabuProperties() {
        return Arrays.<Lecture>asList(leftLecture, rightLecture);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof LectureSwitchMove) {
            LectureSwitchMove other = (LectureSwitchMove) o;
            return new EqualsBuilder()
                    .append(leftLecture, other.leftLecture)
                    .append(rightLecture, other.rightLecture)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(leftLecture)
                .append(rightLecture)
                .toHashCode();
    }

    public String toString() {
        return leftLecture + " <=> " + rightLecture;
    }

}