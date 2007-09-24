package org.drools.solver.examples.travelingtournament.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class TravelingTournament extends AbstractPersistable implements Solution {

    private List<Day> dayList;
    private List<Team> teamList;

    private List<Match> matchList;

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList;
    }

    public List<Match> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<Match> matchSets) {
        this.matchList = matchSets;
    }

    public int getN() {
        return teamList.size();
    }


    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(dayList);
        facts.addAll(teamList);
        facts.addAll(matchList);
        return facts;
    }

    /**
     * Clone will only deep copy the matches
     */
    public TravelingTournament cloneSolution() {
        TravelingTournament clone = new TravelingTournament();
        clone.dayList = dayList;
        clone.teamList = teamList;
        List<Match> clonedMatchList = new ArrayList<Match>(matchList.size());
        for (Match match : matchList) {
            clonedMatchList.add(match.clone());
        }
        clone.matchList = clonedMatchList;
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof TravelingTournament)) {
            return false;
        } else {
            TravelingTournament other = (TravelingTournament) o;
            if (matchList.size() != other.matchList.size()) {
                return false;
            }
            for (Iterator<Match> it = matchList.iterator(), otherIt = other.matchList.iterator(); it.hasNext();) {
                Match match = it.next();
                Match otherMatch = otherIt.next();
                // Not delegated to a custom Match.equals(o) so Matches can be fetched from the WorkingMemory's HashSet
                if (!match.getId().equals(otherMatch.getId()) || !match.getDay().equals(otherMatch.getDay())) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        int hashCode = 0;
        for (Match match : matchList) {
            // Not delegated to a custom Match.hashCode() so Matches can be fetched from the WorkingMemory's HashSet
            hashCode = (hashCode * 31 + match.getId().hashCode()) * 31 + match.getDay().hashCode();
        }
        return hashCode;
    }

}
