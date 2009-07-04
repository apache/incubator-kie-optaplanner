package org.drools.solver.examples.itc2007.examination.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.persistence.AbstractOutputConvertor;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationOutputConvertor extends AbstractOutputConvertor {

    private static final String OUTPUT_FILE_SUFFIX = ".sln";

    public static void main(String[] args) {
        new ExaminationOutputConvertor().convertAll();
    }

    protected String getExampleDirName() {
        return "itc2007/examination";
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public OutputBuilder createOutputBuilder() {
        return new ExaminationOutputBuilder();
    }

    public class ExaminationOutputBuilder extends OutputBuilder {

        private Examination examination;

        public void setSolution(Solution solution) {
            examination = (Examination) solution;
        }

        public void writeSolution() throws IOException {
            Collections.sort(examination.getExamList(), new PersistableIdComparator()); // TODO remove me when obsolete
            for (Exam exam : examination.getExamList()) {
                bufferedWriter.write(exam.getPeriod().getId() + ", " + exam.getRoom().getId() + "\r\n");
            }
        }
        
    }

}