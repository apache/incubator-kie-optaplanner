package org.drools.planner.examples.itc2007.examination.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.itc2007.examination.domain.Examination;
import org.drools.planner.examples.manners2009.domain.Manners2009;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationDaoImpl extends XstreamSolutionDaoImpl {

    public ExaminationDaoImpl() {
        super("itc2007/examination", Examination.class);
    }

}