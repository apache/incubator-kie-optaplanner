package org.drools.solver.examples.itc2007.examination.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationOutputConvertor {

    private static final String INPUT_FILE_SUFFIX = ".xml";
    private static final String OUTPUT_FILE_SUFFIX = ".sln";

    public static void main(String[] args) {
        new ExaminationOutputConvertor().convert();
    }

    private final File inputDir = new File("data/itc2007/examination/solved/");
    private final File outputDir = new File("data/itc2007/examination/output/");

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
                Examination examination = (Examination) solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                writeExamination(examination, outputFile);
            }
        }
    }

    private void writeExamination(Examination examination, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            writeExamination(examination, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    private void writeExamination(Examination examination, BufferedWriter bufferedWriter) throws IOException {
        for (Exam exam : examination.getExamList()) {
            bufferedWriter.write(exam.getPeriod().getId() + ", " + exam.getRoom().getId() + "\r\n");
        }
    }

}