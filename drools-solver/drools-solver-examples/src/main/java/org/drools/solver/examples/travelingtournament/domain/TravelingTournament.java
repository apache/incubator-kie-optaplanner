package org.drools.solver.examples.travelingtournament.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
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
        clone.id = id;
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
                // Notice: we don't use equals()
                if (!match.solutionEquals(otherMatch)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (Match match : matchList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(match.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

}
