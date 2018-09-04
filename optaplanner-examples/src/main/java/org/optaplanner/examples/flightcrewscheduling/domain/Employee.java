/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.flightcrewscheduling.domain;

import java.time.LocalDate;
import java.util.Set;
import java.util.SortedSet;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@PlanningEntity
public class Employee extends AbstractPersistable {

    private static final int MAX_TAXI_TIME = 240;
    private String name;
    private Airport homeAirport;

    private Set<Skill> skillSet;
    private Set<LocalDate> unavailableDaySet;

    /**
     * Sorted by {@link FlightAssignment#DATE_TIME_COMPARATOR}.
     */
    @InverseRelationShadowVariable(sourceVariableName = "employee")
    private SortedSet<FlightAssignment> flightAssignmentSet;

    public Employee() {
    }

    public boolean hasSkill(Skill skill) {
        return skillSet.contains(skill);
    }

    public boolean isAvailable(LocalDate date) {
        return !unavailableDaySet.contains(date);
    }

    public boolean isFirstAssignmentDepartingFromHome() {
        if (flightAssignmentSet.isEmpty()) {
            return true;
        }
        FlightAssignment firstAssignment = flightAssignmentSet.first();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return firstAssignment.getFlight().getDepartureAirport() == homeAirport;
    }

    public boolean isLastAssignmentArrivingAtHome() {
        if (flightAssignmentSet.isEmpty()) {
            return true;
        }
        FlightAssignment lastAssignment = flightAssignmentSet.last();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return lastAssignment.getFlight().getArrivalAirport() == homeAirport;
    }
    
    public ConnectionStatus getConnectionStatus() {
        ConnectionStatus healthCheck = new ConnectionStatus();
        FlightAssignment previousAssignment = null;

        for (FlightAssignment assignment : flightAssignmentSet) {
            if (previousAssignment != null) {
                Airport previousAirport = previousAssignment.getFlight().getArrivalAirport();
                Airport airport = assignment.getFlight().getDepartureAirport();
                if (previousAirport != airport) {
                    Long taxiTimeInMinutes = previousAirport.getTaxiTimeInMinutesTo(airport);
                    if (taxiTimeInMinutes == null || taxiTimeInMinutes > MAX_TAXI_TIME)
                        healthCheck.invalidConnection++;
                    else
                        healthCheck.taxiMinutes += taxiTimeInMinutes == null ? 0 : taxiTimeInMinutes;
                }
            }
            previousAssignment = assignment;
        }
        return healthCheck;
    }

    public long getFlightDurationTotalInMinutes() {
        long total = 0L;
        for (FlightAssignment flightAssignment : flightAssignmentSet) {
            total += flightAssignment.getFlightDurationInMinutes();
        }
        return total;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Subclasses
    // ************************************************************************

    public class ConnectionStatus {
        public long invalidConnection = 0;
        public long taxiMinutes = 0;
    }
    
    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Airport getHomeAirport() {
        return homeAirport;
    }

    public void setHomeAirport(Airport homeAirport) {
        this.homeAirport = homeAirport;
    }

    public Set<Skill> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<Skill> skillSet) {
        this.skillSet = skillSet;
    }

    public Set<LocalDate> getUnavailableDaySet() {
        return unavailableDaySet;
    }

    public void setUnavailableDaySet(Set<LocalDate> unavailableDaySet) {
        this.unavailableDaySet = unavailableDaySet;
    }

    public SortedSet<FlightAssignment> getFlightAssignmentSet() {
        return flightAssignmentSet;
    }

    public void setFlightAssignmentSet(SortedSet<FlightAssignment> flightAssignmentSet) {
        this.flightAssignmentSet = flightAssignmentSet;
    }

}
