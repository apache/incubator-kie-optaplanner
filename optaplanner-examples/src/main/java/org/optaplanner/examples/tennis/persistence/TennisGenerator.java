/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.tennis.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.tennis.domain.Day;
import org.optaplanner.examples.tennis.domain.Team;
import org.optaplanner.examples.tennis.domain.TeamAssignment;
import org.optaplanner.examples.tennis.domain.TennisSolution;
import org.optaplanner.examples.tennis.domain.UnavailabilityPenalty;

public class TennisGenerator extends LoggingMain {

    public static void main(String[] args) {
        new TennisGenerator().generate();
    }

    protected final SolutionDao solutionDao;
    protected final File outputDir;

    public TennisGenerator() {
        solutionDao = new TennisDao();
        outputDir = new File(solutionDao.getDataDir(), "unsolved");
    }

    public void generate() {
        String inputId = "munich-7teams";
        File outputFile = new File(outputDir, inputId + ".xml");
        TennisSolution tennisSolution = createTennisSolution(inputId);
        solutionDao.writeSolution(tennisSolution, outputFile);
    }

    private TennisSolution createTennisSolution(String inputId) {
        TennisSolution tennisSolution = new TennisSolution();
        tennisSolution.setId(0L);

        List<Team> teamList = new ArrayList<Team>();
        teamList.add(new Team(0L, "Micha"));
        teamList.add(new Team(1L, "Angelika"));
        teamList.add(new Team(2L, "Katrin"));
        teamList.add(new Team(3L, "Susi"));
        teamList.add(new Team(4L, "Irene"));
        teamList.add(new Team(5L, "Kristina"));
        teamList.add(new Team(6L, "Tobias"));
        tennisSolution.setTeamList(teamList);

        List<Day> dayList = new ArrayList<Day>();
        for (int i = 0; i < 18; i++) {
            dayList.add(new Day(i, i));
        }
        tennisSolution.setDayList(dayList);

        List<UnavailabilityPenalty> unavailabilityPenaltyList = new ArrayList<UnavailabilityPenalty>();
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(4), dayList.get(0)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(6), dayList.get(1)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(2), dayList.get(2)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(4), dayList.get(3)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(4), dayList.get(5)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(2), dayList.get(6)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(1), dayList.get(8)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(2), dayList.get(9)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(4), dayList.get(10)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(4), dayList.get(11)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(6), dayList.get(12)));
        unavailabilityPenaltyList.add(new UnavailabilityPenalty(teamList.get(5), dayList.get(15)));
        tennisSolution.setUnavailabilityPenaltyList(unavailabilityPenaltyList);

        List<TeamAssignment> teamAssignmentList = new ArrayList<TeamAssignment>();
        long id = 0L;
        for (Day day : dayList) {
            for (int i = 0; i < 4; i++) {
                teamAssignmentList.add(new TeamAssignment(id, day, i));
                id++;
            }
        }
        tennisSolution.setTeamAssignmentList(teamAssignmentList);

        BigInteger possibleSolutionSize = BigInteger.valueOf(teamList.size()).pow(
                teamAssignmentList.size());
        logger.info("Tennis {} has {} teams, {} days, {} unavailabilityPenalties and {} teamAssignments"
                + " with a search space of {}.",
                inputId, teamList.size(), dayList.size(), unavailabilityPenaltyList.size(), teamAssignmentList.size(),
                AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return tennisSolution;
    }

}
