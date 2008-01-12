package org.drools.solver.examples.itc2007.examination.solver;

import org.drools.solver.core.solution.initializer.AbstractStartingSolutionInitializer;
import org.drools.solver.examples.itc2007.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationStartingSolutionInitializer extends AbstractStartingSolutionInitializer {

    public void intializeSolution() {
        Examination examination = (Examination) solver.getEvaluationHandler().getSolution();
        if (examination.getExamList() == null) {
            // TODO do stuff
        }
    }

}
