package org.drools.solver.examples.travelingtournament.persistence.simple;

import java.io.File;
import java.util.List;

import org.drools.solver.examples.travelingtournament.domain.Day;
import org.drools.solver.examples.travelingtournament.domain.Match;
import org.drools.solver.examples.travelingtournament.domain.TravelingTournament;
import org.drools.solver.examples.travelingtournament.persistence.TravelingTournamentInputConvertor;
import org.drools.solver.examples.common.persistence.AbstractInputConvertor;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentInputConvertor extends TravelingTournamentInputConvertor {

    public static void main(String[] args) {
        new SimpleTravelingTournamentInputConvertor().convertAll();
    }

    private final File outputDir = new File("data/travelingtournament/simple/unsolved/");

    @Override
    protected File getOutputDir() {
        return outputDir;
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
