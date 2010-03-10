package org.drools.planner.examples.curriculumcourse.persistence;

import java.io.IOException;

import org.drools.planner.examples.common.persistence.AbstractTxtOutputConverter;
import org.drools.planner.examples.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.planner.examples.curriculumcourse.domain.Lecture;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseOutputConverter extends AbstractTxtOutputConverter {

    private static final String OUTPUT_FILE_SUFFIX = ".sol";

    public static void main(String[] args) {
        new CurriculumCourseOutputConverter().convertAll();
    }

    public CurriculumCourseOutputConverter() {
        super(new CurriculumCourseDaoImpl());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public TxtOutputBuilder createTxtOutputBuilder() {
        return new CurriculumCourseOutputBuilder();
    }

    public class CurriculumCourseOutputBuilder extends TxtOutputBuilder {

        private CurriculumCourseSchedule schedule;

        public void setSolution(Solution solution) {
            schedule = (CurriculumCourseSchedule) solution;
        }

        public void writeSolution() throws IOException {
            for (Lecture lecture : schedule.getLectureList()) {
                bufferedWriter.write(lecture.getCourse().getCode()
                        + " r" + lecture.getRoom().getCode()
                        + " " + lecture.getPeriod().getDay().getDayIndex()
                        + " " + lecture.getPeriod().getTimeslot().getTimeslotIndex() + "\r\n");
            }
        }
    }

}
