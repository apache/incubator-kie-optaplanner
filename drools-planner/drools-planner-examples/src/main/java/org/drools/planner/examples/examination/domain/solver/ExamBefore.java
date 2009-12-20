package org.drools.planner.examples.examination.domain.solver;

import java.io.Serializable;
import java.util.Set;

import org.drools.planner.examples.examination.domain.Exam;

/**
 * Calculated during initialization, not used for score calculation, used for move creation.
 * @author Geoffrey De Smet
 */
public class ExamBefore implements Serializable {

    private Set<Exam> afterExamSet;

    public ExamBefore(Set<Exam> afterExamSet) {
        this.afterExamSet = afterExamSet;
    }

    public Set<Exam> getAfterExamSet() {
        return afterExamSet;
    }

    public void setAfterExamSet(Set<Exam> afterExamSet) {
        this.afterExamSet = afterExamSet;
    }

}