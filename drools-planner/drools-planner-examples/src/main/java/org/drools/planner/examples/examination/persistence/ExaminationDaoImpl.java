package org.drools.planner.examples.examination.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationDaoImpl extends XstreamSolutionDaoImpl {

    public ExaminationDaoImpl() {
        super("examination", Examination.class);
    }

}