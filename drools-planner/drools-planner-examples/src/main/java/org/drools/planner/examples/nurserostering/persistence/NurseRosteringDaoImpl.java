package org.drools.planner.examples.nurserostering.persistence;

import org.drools.planner.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringDaoImpl extends XstreamSolutionDaoImpl {

    public NurseRosteringDaoImpl() {
        super("nurserostering", NurseRoster.class);
    }

}
