package org.drools.solver.examples.itc2007.examination.solver.move;

import org.drools.WorkingMemory;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationMoveHelper {

    public static void movePeriod(WorkingMemory workingMemory, Exam exam, Period period) {
        exam.setPeriod(period);
        workingMemory.update(workingMemory.getFactHandle(exam), exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void moveRoom(WorkingMemory workingMemory, Exam exam, Room room) {
        exam.setRoom(room);
        workingMemory.update(workingMemory.getFactHandle(exam), exam);
    }

    public static void moveExam(WorkingMemory workingMemory, Exam exam, Period period, Room room) {
        exam.setPeriod(period);
        exam.setRoom(room);
        workingMemory.update(workingMemory.getFactHandle(exam), exam);
        movePeriodCoincidene(workingMemory, exam, period);
    }

    public static void movePeriodCoincidene(WorkingMemory workingMemory, Exam exam, Period period) {
        if (exam.getExamCoincidence() != null) {
            for (Exam coincidenceExam : exam.getExamCoincidence().getCoincidenceExamSet()) {
                if (!exam.equals(coincidenceExam)) {
                    coincidenceExam.setPeriod(period);
                    workingMemory.update(workingMemory.getFactHandle(coincidenceExam), coincidenceExam);
                }
            }
        }
    }

    private ExaminationMoveHelper() {
    }

}
