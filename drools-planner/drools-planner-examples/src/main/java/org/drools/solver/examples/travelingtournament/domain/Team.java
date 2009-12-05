package org.drools.solver.examples.travelingtournament.domain;

import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Team extends AbstractPersistable implements Comparable<Team> {

    private String name;
    private Map<Team, Integer> distanceToTeamMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Team, Integer> getDistanceToTeamMap() {
        return distanceToTeamMap;
    }

    public void setDistanceToTeamMap(Map<Team, Integer> distanceToTeamMap) {
        this.distanceToTeamMap = distanceToTeamMap;
    }

    public int compareTo(Team other) {
        return new CompareToBuilder()
                .append(name, other.name)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return getName();
    }

}
