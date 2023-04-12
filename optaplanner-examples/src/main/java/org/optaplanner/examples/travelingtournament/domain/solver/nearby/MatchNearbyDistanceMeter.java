package org.optaplanner.examples.travelingtournament.domain.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.examples.travelingtournament.domain.Match;

public class MatchNearbyDistanceMeter implements NearbyDistanceMeter<Match, Match> {

    @Override
    public double getNearbyDistance(Match origin, Match destination) {
        return origin.getHomeTeam().getDistance(destination.getHomeTeam())
                + origin.getHomeTeam().getDistance(destination.getAwayTeam())
                + origin.getAwayTeam().getDistance(destination.getAwayTeam())
                + origin.getAwayTeam().getDistance(destination.getHomeTeam());
    }

}