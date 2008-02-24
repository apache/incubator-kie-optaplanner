package org.drools.solver.examples.itc2007.examination.solver.move;

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationMoveHelper {

    public static void movePeriod(WorkingMemory workingMemory, Exam exam, Period period) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        workingMemory.modifyRetract(factHandle);
        exam.setPeriod(period);
        workingMemory.modifyInsert(factHandle, exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void moveRoom(WorkingMemory workingMemory, Exam exam, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        workingMemory.modifyRetract(factHandle);
        exam.setRoom(room);
        workingMemory.modifyInsert(factHandle, exam);
    }

    public static void moveExam(WorkingMemory workingMemory, Exam exam, Period period, Room room) {
        FactHandle factHandle = workingMemory.getFactHandle(exam);
        workingMemory.modifyRetract(factHandle);
        exam.setPeriod(period);
        exam.setRoom(room);
        workingMemory.modifyInsert(factHandle, exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void movePeriodCoincidene(WorkingMemory workingMemory, Exam exam, Period period) {
        if (exam.getExamCoincidence() != null) {
            for (Exam coincidenceExam : exam.getExamCoincidence().getCoincidenceExamSet()) {
                if (!exam.equals(coincidenceExam)) {
                    FactHandle factHandle = workingMemory.getFactHandle(coincidenceExam);
                    workingMemory.modifyRetract(factHandle);
                    coincidenceExam.setPeriod(period);
                    workingMemory.modifyInsert(factHandle, coincidenceExam);
                }
            }
        }
    }

    private ExaminationMoveHelper() {
    }

}
