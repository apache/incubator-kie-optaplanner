package org.drools.planner.examples.itc2007.examination.solver.move;

import org.drools.WorkingMemory;
import org.drools.runtime.rule.FactHandle;
import org.drools.planner.examples.itc2007.examination.domain.Exam;
import org.drools.planner.examples.itc2007.examination.domain.Period;
import org.drools.planner.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationMoveHelper {

    public static void movePeriod(WorkingMemory workingMemory, Exam exam, Period period) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        exam.setPeriod(period);
        workingMemory.update(factHandle, exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void moveRoom(WorkingMemory workingMemory, Exam exam, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        exam.setRoom(room);
        workingMemory.update(factHandle, exam);
    }

    public static void moveExam(WorkingMemory workingMemory, Exam exam, Period period, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        exam.setPeriod(period);
        exam.setRoom(room);
        workingMemory.update(factHandle, exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void movePeriodCoincidene(WorkingMemory workingMemory, Exam exam, Period period) {
        if (exam.getExamCoincidence() != null) {
            for (Exam coincidenceExam : exam.getExamCoincidence().getCoincidenceExamSet()) {
                if (!exam.equals(coincidenceExam)) {
                    FactHandle factHandle = workingMemory.getFactHandle(coincidenceExam);                   
                    coincidenceExam.setPeriod(period);
                    workingMemory.update(factHandle, coincidenceExam);
                }
            }
        }
    }

    private ExaminationMoveHelper() {
    }

}
