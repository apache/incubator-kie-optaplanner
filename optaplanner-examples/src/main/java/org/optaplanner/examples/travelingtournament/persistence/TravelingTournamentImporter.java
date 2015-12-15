/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.travelingtournament.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplanner.examples.travelingtournament.domain.Day;
import org.optaplanner.examples.travelingtournament.domain.Match;
import org.optaplanner.examples.travelingtournament.domain.Team;
import org.optaplanner.examples.travelingtournament.domain.TravelingTournament;

public class TravelingTournamentImporter extends AbstractTxtSolutionImporter {

    public static void main(String[] args) {
        new TravelingTournamentImporter().convertAll();
    }

    public TravelingTournamentImporter() {
        super(new TravelingTournamentDao());
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new TravelingTournamentInputBuilder();
    }

    public static class TravelingTournamentInputBuilder extends TxtInputBuilder {

        public Solution readSolution() throws IOException {
            TravelingTournament travelingTournament = new TravelingTournament();
            travelingTournament.setId(0L);
            int n = readN();
            readTeamList(travelingTournament, n);
            createDayList(travelingTournament, n);
            List<List<Integer>> outerDistanceList = readOuterDistanceList(travelingTournament);
            // TODO setting the distances should be a separate method
            createMatchListAndSetDistancesInTeamList(travelingTournament, outerDistanceList);
            initializeMatchDays(travelingTournament);
            BigInteger possibleSolutionSize = factorial(2 * (n - 1)).pow(n / 2);
            logger.info("TravelingTournament {} has {} days, {} teams and {} matches with a search space of {}.",
                    getInputId(),
                    travelingTournament.getDayList().size(),
                    travelingTournament.getTeamList().size(),
                    travelingTournament.getMatchList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            return travelingTournament;
        }

        private int readN() throws IOException {
            return Integer.parseInt(bufferedReader.readLine());
        }

        private void readTeamList(TravelingTournament travelingTournament, int n) throws IOException {
            List<Team> teamList = new ArrayList<Team>();
            for (int i = 0; i < n; i++) {
                Team team = new Team();
                team.setId((long) i);
                team.setName(bufferedReader.readLine());
                team.setDistanceToTeamMap(new HashMap<Team, Integer>());
                teamList.add(team);
            }
            travelingTournament.setTeamList(teamList);
        }

        private List<List<Integer>> readOuterDistanceList(TravelingTournament travelingTournament) throws IOException {
            List<List<Integer>> outerDistanceList = new ArrayList<List<Integer>>();
            String line = bufferedReader.readLine();
            while (line != null && !line.replaceAll("\\s+", "").equals("")) {
                StringTokenizer tokenizer = new StringTokenizer(line.replaceAll("\\s+", " ").trim());
                List<Integer> innerDistanceList = new ArrayList<Integer>();
                while (tokenizer.hasMoreTokens()) {
                    int distance = Integer.parseInt(tokenizer.nextToken());
                    innerDistanceList.add(distance);
                }
                outerDistanceList.add(innerDistanceList);
                line = bufferedReader.readLine();
            }
            return outerDistanceList;
        }

        private void createDayList(TravelingTournament travelingTournament, int n) {
            List<Day> dayList = new ArrayList<Day>();
            int daySize = (n - 1) * 2; // Play vs each team (except itself) twice (home and away)
            Day previousDay = null;
            for (int i = 0; i < daySize; i++) {
                Day day = new Day();
                day.setId((long) i);
                day.setIndex(i);
                dayList.add(day);
                if (previousDay != null) {
                    previousDay.setNextDay(day);
                }
                previousDay = day;
            }
            travelingTournament.setDayList(dayList);
        }

        private void createMatchListAndSetDistancesInTeamList(TravelingTournament travelingTournament,
                List<List<Integer>> outerDistanceList) {
            List<Team> teamList = travelingTournament.getTeamList();
            List<Match> matchList = new ArrayList<Match>();
            int i = 0;
            long matchId = 0;
            for (Team homeTeam : teamList) {
                int j = 0;
                for (Team awayTeam : teamList) {
                    int distance = outerDistanceList.get(i).get(j);
                    homeTeam.getDistanceToTeamMap().put(awayTeam, distance);
                    if (i != j) {
                        Match match = new Match();
                        match.setId(matchId);
                        matchId++;
                        match.setHomeTeam(homeTeam);
                        match.setAwayTeam(awayTeam);
                        matchList.add(match);
                    }
                    j++;
                }
                i++;
            }
            travelingTournament.setMatchList(matchList);
        }

        /**
         * Canonical pattern initialization (see papers).
         * @param travelingTournament the traveling tournament
         */
        protected void initializeMatchDays(TravelingTournament travelingTournament) {
            int n = travelingTournament.getN();
            for (int i = 0; i < (n - 1); i++) {
                initializeMatchPairs(travelingTournament, (n - 1), i, i);
            }
            for (int startA = 1, startB = (n - 2); startA < (n - 1); startA += 2, startB -= 2) {
                for (int i = 0; i < (n - 1); i++) {
                    int a = (startA + i) % (n - 1);
                    int b = (startB + i) % (n - 1);
                    initializeMatchPairs(travelingTournament, a, b, i);
                }
            }
        }

        private void initializeMatchPairs(TravelingTournament travelingTournament, int a, int b, int i) {
            if ((i % 6) >= 3) { // Might not be a 100% the canonical pattern
                // Swap them
                int oldA = a;
                a = b;
                b = oldA;
            }
            Team aTeam = travelingTournament.getTeamList().get(a);
            Team bTeam = travelingTournament.getTeamList().get(b);
            Match m1 = findMatch(travelingTournament.getMatchList(), aTeam, bTeam);
            m1.setDay(travelingTournament.getDayList().get(i));
            Match m2 = findMatch(travelingTournament.getMatchList(), bTeam, aTeam);
            m2.setDay(travelingTournament.getDayList().get(i + travelingTournament.getN() - 1));
        }

        private Match findMatch(List<Match> matchList, Team homeTeam, Team awayTeam) {
            for (Match match : matchList) {
                if (match.getHomeTeam().equals(homeTeam) && match.getAwayTeam().equals(awayTeam)) {
                    return match;
                }
            }
            throw new IllegalStateException("Nothing found for: " + homeTeam + " and " + awayTeam);
        }

    }

}
