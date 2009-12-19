package org.drools.planner.examples.curriculumcourse.solver.move;


import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import org.drools.planner.examples.curriculumcourse.domain.Lecture;
import org.drools.planner.examples.curriculumcourse.domain.Period;
import org.drools.planner.examples.curriculumcourse.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseMoveHelper {

    public static void movePeriod(WorkingMemory workingMemory, Lecture lecture, Period period) {
        FactHandle factHandle = workingMemory.getFactHandle(lecture);
        lecture.setPeriod(period);
        workingMemory.update(factHandle, lecture);
    }

    public static void moveRoom(WorkingMemory workingMemory, Lecture lecture, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(lecture);
        lecture.setRoom(room);
        workingMemory.update(factHandle, lecture);
    }

    public static void moveLecture(WorkingMemory workingMemory, Lecture lecture, Period period, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(lecture);
        lecture.setPeriod(period);
        lecture.setRoom(room);
        workingMemory.update(factHandle, lecture);
    }

    private CurriculumCourseMoveHelper() {
    }

}