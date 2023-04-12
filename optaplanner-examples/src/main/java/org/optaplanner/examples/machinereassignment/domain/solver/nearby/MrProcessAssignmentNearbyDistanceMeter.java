package org.optaplanner.examples.machinereassignment.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.machinereassignment.domain.MrProcessAssignment;
import org.optaplanner.examples.machinereassignment.domain.MrProcessRequirement;

public class MrProcessAssignmentNearbyDistanceMeter implements NearbyDistanceMeter<MrProcessAssignment, MrProcessAssignment> {

    @Override
    public double getNearbyDistance(MrProcessAssignment origin, MrProcessAssignment destination) {
        int commonRequires = 0;
        for (MrProcessRequirement originRequirement : origin.getProcess().getProcessRequirementList()) {
            for (MrProcessRequirement destinationRequirement : destination.getProcess().getProcessRequirementList()) {
                if (originRequirement.getId() == destinationRequirement.getId()) {
                    commonRequires++;
                    break;
                }
            }
        }
        return origin.getProcess().getProcessRequirementList().size() - commonRequires;
    }

}
