package org.drools.planner.examples.travelingtournament.persistence.simple;

import java.io.File;
import java.util.List;

import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;
import org.drools.planner.examples.travelingtournament.persistence.TravelingTournamentInputConvertor;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentInputConvertor extends TravelingTournamentInputConvertor {

    public static void main(String[] args) {
        new SimpleTravelingTournamentInputConvertor().convertAll();
    }

    public SimpleTravelingTournamentInputConvertor() {
        super(new SimpleTravelingTournamentDaoImpl());
    }

    public InputBuilder createInputBuilder() {
        return new SimpleTravelingTournament();
    }

    public class SimpleTravelingTournament extends TravelingTournamentInputBuilder {

        protected void initializeMatchDays(TravelingTournament travelingTournament) {
            List<Match> matchList = travelingTournament.getMatchList();
            List<Day> dayList = travelingTournament.getDayList();
            for (Match match : matchList) {
                match.setDay(dayList.get(0));
            }
        }

    }

}
