package org.drools.planner.examples.travelingtournament.solver.simple.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.planner.core.move.Move;
import org.drools.planner.core.move.factory.CachedMoveFactory;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;
import org.drools.planner.examples.travelingtournament.solver.simple.move.DayChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        List<Move> moveList = new ArrayList<Move>();
        TravelingTournament travelingTournament = (TravelingTournament) solution;
        for (Match match : travelingTournament.getMatchList()) {
            for (Day day : travelingTournament.getDayList()) {
                moveList.add(new DayChangeMove(match, day));
            }
        }
        return moveList;
    }

}
