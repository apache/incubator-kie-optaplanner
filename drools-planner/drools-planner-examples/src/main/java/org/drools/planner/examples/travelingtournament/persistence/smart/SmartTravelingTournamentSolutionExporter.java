package org.drools.planner.examples.travelingtournament.persistence.smart;

import java.io.IOException;

import org.drools.planner.examples.common.persistence.AbstractTxtSolutionExporter;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;
import org.drools.planner.examples.travelingtournament.domain.Team;
import org.drools.planner.examples.travelingtournament.domain.Match;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentSolutionExporter extends AbstractTxtSolutionExporter {

    private static final String OUTPUT_FILE_SUFFIX = ".trick.txt";

    public static void main(String[] args) {
        new SmartTravelingTournamentSolutionExporter().convertAll();
    }

    public SmartTravelingTournamentSolutionExporter() {
        super(new SmartTravelingTournamentDaoImpl());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public TxtOutputBuilder createTxtOutputBuilder() {
        return new SmartTravelingTournamentOutputBuilder();
    }

    public class SmartTravelingTournamentOutputBuilder extends TxtOutputBuilder {

        private TravelingTournament travelingTournament;

        public void setSolution(Solution solution) {
            travelingTournament = (TravelingTournament) solution;
        }

        public void writeSolution() throws IOException {
            int maximumTeamNameLength = 0;
            for (Team team : travelingTournament.getTeamList()) {
                if (team.getName().length() > maximumTeamNameLength) {
                    maximumTeamNameLength = team.getName().length();
                }
            }
            for (Team team : travelingTournament.getTeamList()) {
                bufferedWriter.write(String.format("%-" + (maximumTeamNameLength + 3) + "s", team.getName()));
            }
            bufferedWriter.write("\n");
            for (Team team : travelingTournament.getTeamList()) {
                bufferedWriter.write(String.format("%-" + (maximumTeamNameLength + 3) + "s", team.getName().replaceAll("[\\w\\d]", "-")));
            }
            bufferedWriter.write("\n");
            for (Day day : travelingTournament.getDayList()) {
                for (Team team : travelingTournament.getTeamList()) {
                    // this could be put in a hashmap first for performance
                    boolean opponentIsHome = false;
                    Team opponentTeam = null;
                    for (Match match : travelingTournament.getMatchList()) {
                        if (match.getDay().equals(day)) {
                            if (match.getHomeTeam().equals(team)) {
                                opponentIsHome = false;
                                opponentTeam = match.getAwayTeam();
                            } else  if (match.getAwayTeam().equals(team)) {
                                opponentIsHome = true;
                                opponentTeam = match.getHomeTeam();
                            }
                        }
                    }
                    String opponentName = (opponentIsHome ? "@" : "") + opponentTeam.getName();
                    bufferedWriter.write(String.format("%-" + (maximumTeamNameLength + 3) + "s", opponentName));
                }
                bufferedWriter.write("\n");
            }
        }

    }

}
