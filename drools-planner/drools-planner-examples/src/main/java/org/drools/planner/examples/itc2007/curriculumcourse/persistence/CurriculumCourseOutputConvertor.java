package org.drools.planner.examples.itc2007.curriculumcourse.persistence;

import java.io.IOException;

import org.drools.planner.examples.common.persistence.AbstractOutputConvertor;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.planner.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseOutputConvertor extends AbstractOutputConvertor {

    private static final String OUTPUT_FILE_SUFFIX = ".sol";

    public static void main(String[] args) {
        new CurriculumCourseOutputConvertor().convertAll();
    }

    public CurriculumCourseOutputConvertor() {
        super(new CurriculumCourseDaoImpl());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public OutputBuilder createOutputBuilder() {
        return new CurriculumCourseOutputBuilder();
    }

    public class CurriculumCourseOutputBuilder extends OutputBuilder {

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