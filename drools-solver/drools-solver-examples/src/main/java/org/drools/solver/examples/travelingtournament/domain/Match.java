package org.drools.solver.examples.travelingtournament.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Match extends AbstractPersistable implements Comparable<Match> {

    private Team homeTeam;
    private Team awayTeam;

    private Day day;

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int compareTo(Match other) {
        return new CompareToBuilder()
                .append(day, other.day)
                .append(homeTeam, other.homeTeam)
                .append(awayTeam, other.awayTeam)
                .append(id, other.id)
                .toComparison();
    }

    public Match clone() {
        Match clone = new Match();
        clone.id = id;
        clone.homeTeam = homeTeam;
        clone.awayTeam = awayTeam;
        clone.day = day;
        return clone;
    }

    public String toString() {
        return super.toString() + " " + homeTeam + " + " + awayTeam + " @ " + day;
    }

}
