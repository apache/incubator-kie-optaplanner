package org.optaplanner.examples.nqueens.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.nqueens.domain.Queen;

import static java.lang.Math.abs;

public class QueenNearbyDistance implements NearbyDistanceMeter<Queen, Queen> {

    @Override
    public double getNearbyDistance(Queen origin, Queen destination) {
        return abs(origin.getRowIndex() - destination.getRowIndex()) + abs(origin.getColumnIndex() - destination.getColumnIndex());
    }

}
