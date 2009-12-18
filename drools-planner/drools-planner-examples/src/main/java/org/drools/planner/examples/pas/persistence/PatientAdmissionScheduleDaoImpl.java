package org.drools.planner.examples.pas.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.manners2009.domain.Manners2009;
import org.drools.planner.examples.pas.domain.PatientAdmissionSchedule;

/**
 * @author Geoffrey De Smet
 */
public class PatientAdmissionScheduleDaoImpl extends XstreamSolutionDaoImpl {

    public PatientAdmissionScheduleDaoImpl() {
        super("pas", PatientAdmissionSchedule.class);
    }

}