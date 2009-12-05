package org.drools.solver.examples.travelingtournament.persistence.smart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.domain.PersistableIdComparator;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.examples.common.persistence.AbstractOutputConvertor;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.travelingtournament.domain.TravelingTournament;
import org.drools.solver.examples.travelingtournament.domain.Team;
import org.drools.solver.examples.travelingtournament.domain.Match;
import org.drools.solver.examples.travelingtournament.domain.Day;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentOutputConvertor extends AbstractOutputConvertor {

    private static final String OUTPUT_FILE_SUFFIX = ".trick.txt";

    public static void main(String[] args) {
        new SmartTravelingTournamentOutputConvertor().convertAll();
    }

    private final File inputDir = new File("data/travelingtournament/smart/solved/");

    protected String getExampleDirName() {
        return "travelingtournament/smart";
    }

    @Override
    protected File getInputDir() {
        return inputDir;
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public OutputBuilder createOutputBuilder() {
        return new SmartTravelingTournamentOutputBuilder();
    }

    public class SmartTravelingTournamentOutputBuilder extends OutputBuilder {

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