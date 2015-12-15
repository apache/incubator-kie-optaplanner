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

package org.optaplanner.examples.tennis.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.tennis.domain.solver.MovableTeamAssignmentSelectionFilter;

@PlanningEntity(movableEntitySelectionFilter = MovableTeamAssignmentSelectionFilter.class)
@XStreamAlias("TennisTeamAssignment")
public class TeamAssignment extends AbstractPersistable {

    private Day day;
    private int indexInDay;
    private boolean locked;

    // planning variable
    private Team team;

    public TeamAssignment() {
    }

    public TeamAssignment(long id, Day day, int indexInDay) {
        super(id);
        this.day = day;
        this.indexInDay = indexInDay;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int getIndexInDay() {
        return indexInDay;
    }

    public void setIndexInDay(int indexInDay) {
        this.indexInDay = indexInDay;
    }
    /**
     * @return true if immovable planning entity
     * @see MovableTeamAssignmentSelectionFilter
     */
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @PlanningVariable(valueRangeProviderRefs = {"teamRange"})
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "Day-" + day.getDateIndex() + "(" + indexInDay + ")";
    }

}
