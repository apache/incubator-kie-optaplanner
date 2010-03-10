package org.drools.planner.examples.examination.persistence;

import java.io.IOException;
import java.util.Collections;

import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.common.persistence.AbstractTxtOutputConverter;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Examination;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationOutputConverter extends AbstractTxtOutputConverter {

    private static final String OUTPUT_FILE_SUFFIX = ".sln";

    public static void main(String[] args) {
        new ExaminationOutputConverter().convertAll();
    }

    public ExaminationOutputConverter() {
        super(new ExaminationDaoImpl());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public TxtOutputBuilder createTxtOutputBuilder() {
        return new ExaminationOutputBuilder();
    }

    public class ExaminationOutputBuilder extends TxtOutputBuilder {

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
