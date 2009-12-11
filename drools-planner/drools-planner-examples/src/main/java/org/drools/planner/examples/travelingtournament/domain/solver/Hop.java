package org.drools.planner.examples.travelingtournament.domain.solver;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.travelingtournament.domain.Team;

/**
 * @author Geoffrey De Smet
 */
public class Hop implements Serializable {

    private Team team;
    private Team fromTeam;
    private Team toTeam;

    public Hop(Team team, Team fromTeam, Team toTeam) {
        this.team = team;
        this.fromTeam = fromTeam;
        this.toTeam = toTeam;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getFromTeam() {
        return fromTeam;
    }

    public void setFromTeam(Team fromTeam) {
        this.fromTeam = fromTeam;
    }

    public Team getToTeam() {
        return toTeam;
    }

    public void setToTeam(Team toTeam) {
        this.toTeam = toTeam;
    }


    public int getDistance() {
        Map<Team,Integer> distanceToTeamMap = fromTeam.getDistanceToTeamMap();
        return distanceToTeamMap.get(toTeam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Hop) {
            Hop other = (Hop) o;
            return new EqualsBuilder()
                    .append(team, other.team)
                    .append(fromTeam, other.fromTeam)
                    .append(toTeam, other.toTeam)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(team)
                .append(fromTeam)
                .append(toTeam)
                .toHashCode();
    }
    
}
