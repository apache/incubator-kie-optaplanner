package org.optaplanner.examples.nurserostering.domain.solver.nearby;

import static java.lang.Math.abs;

import java.time.Period;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.nurserostering.domain.ShiftAssignment;

public class ShiftAssignmentNearbyDistanceMeter implements NearbyDistanceMeter<ShiftAssignment, ShiftAssignment> {

    @Override
    public double getNearbyDistance(ShiftAssignment origin, ShiftAssignment destination) {
        Period period = Period.between(origin.getShiftDate().getDate(), destination.getShiftDate().getDate());
        return abs(period.getDays() + 30 * period.getMonths() + 365 * period.getYears());
    }

}
