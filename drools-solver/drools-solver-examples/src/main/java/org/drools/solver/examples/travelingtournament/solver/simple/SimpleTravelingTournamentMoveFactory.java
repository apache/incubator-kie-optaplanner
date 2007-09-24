package org.drools.solver.examples.travelingtournament.solver.simple;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.decider.selector.CachedMoveListMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.travelingtournament.domain.Day;
import org.drools.solver.examples.travelingtournament.domain.Match;
import org.drools.solver.examples.travelingtournament.domain.TravelingTournament;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentMoveFactory extends CachedMoveListMoveFactory {

    public List<Move> createMoveList(Solution solution) {
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
