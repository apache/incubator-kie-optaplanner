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
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.travelingtournament.domain.TravelingTournament;
import org.drools.solver.examples.travelingtournament.domain.Team;
import org.drools.solver.examples.travelingtournament.domain.Match;
import org.drools.solver.examples.travelingtournament.domain.Day;

/**
 * @author Geoffrey De Smet
 */
public class SmartTravelingTournamentOutputConvertor extends LoggingMain {

    private static final String INPUT_FILE_SUFFIX = ".xml";
    private static final String OUTPUT_FILE_SUFFIX = ".trick.txt";

    public static void main(String[] args) {
        new SmartTravelingTournamentOutputConvertor().convert();
    }

    private final File inputDir = new File("data/travelingtournament/smart/solved/");
    private final File outputDir = new File("data/travelingtournament/output/");

    public void convert() {
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(INPUT_FILE_SUFFIX)) {
                TravelingTournament travelingTournament = (TravelingTournament) solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0, inputFileName.length() - INPUT_FILE_SUFFIX.length())
                        + OUTPUT_FILE_SUFFIX;
                File outputFile = new File(outputDir, outputFileName);
                writeTravelingTournament(travelingTournament, outputFile);
            }
        }
    }

    public void writeTravelingTournament(TravelingTournament travelingTournament, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            writeTravelingTournament(travelingTournament, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    public void writeTravelingTournament(TravelingTournament travelingTournament, BufferedWriter bufferedWriter)
            throws IOException {
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