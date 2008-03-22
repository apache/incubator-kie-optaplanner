package org.drools.solver.examples.itc2007.curriculumcourse.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.CurriculumCourseSchedule;
import org.drools.solver.examples.itc2007.curriculumcourse.domain.Lecture;

/**
 * @author Geoffrey De Smet
 */
public class CurriculumCourseOutputConvertor extends LoggingMain {

    private static final String INPUT_FILE_SUFFIX = ".xml";
    private static final String OUTPUT_FILE_SUFFIX = ".sol";

    public static void main(String[] args) {
        new CurriculumCourseOutputConvertor().convert();
    }

    private final File inputDir = new File("data/itc2007/curriculumcourse/solved/");
    private final File outputDir = new File("data/itc2007/curriculumcourse/output/");

    public void convert() {
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(INPUT_FILE_SUFFIX)) {
                CurriculumCourseSchedule schedule = (CurriculumCourseSchedule) solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                writeCurriculumCourseSchedule(schedule, outputFile);
            }
        }
    }

    public void writeCurriculumCourseSchedule(CurriculumCourseSchedule schedule, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            writeExamination(schedule, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    public void writeExamination(CurriculumCourseSchedule schedule, BufferedWriter bufferedWriter) throws IOException {
        for (Lecture lecture : schedule.getLectureList()) {
            bufferedWriter.write(lecture.getCourse().getCode() + " " + lecture.getRoom().getCode()
                    + " " + lecture.getPeriod().getDay().getDayIndex()
                    + " " + lecture.getPeriod().getTimeslot().getTimeslotIndex() + "\n");
        }
    }

}