package org.drools.solver.examples.itc2007.curriculumcourse.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.persistence.AbstractOutputConvertor;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseOutputConvertor extends AbstractOutputConvertor {

    private static final String OUTPUT_FILE_SUFFIX = ".sol";

    public static void main(String[] args) {
        new CurriculumCourseOutputConvertor().convertAll();
    }

    protected String getExampleDirName() {
        return "itc2007/curriculumcourse";
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