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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@PlanningSolution
@XStreamAlias("TennisSolution")
public class TennisSolution extends AbstractPersistable implements Solution<HardMediumSoftScore> {

    private List<Team> teamList;
    private List<Day> dayList;
    private List<UnavailabilityPenalty> unavailabilityPenaltyList;

    private List<TeamAssignment> teamAssignmentList;

    private HardMediumSoftScore score;

    @ValueRangeProvider(id = "teamRange")
    public List<Team> getTeamList() {
        return teamList;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList;
    }

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    public List<UnavailabilityPenalty> getUnavailabilityPenaltyList() {
        return unavailabilityPenaltyList;
    }

    public void setUnavailabilityPenaltyList(List<UnavailabilityPenalty> unavailabilityPenaltyList) {
        this.unavailabilityPenaltyList = unavailabilityPenaltyList;
    }

    @PlanningEntityCollectionProperty
    public List<TeamAssignment> getTeamAssignmentList() {
        return teamAssignmentList;
    }

    public void setTeamAssignmentList(List<TeamAssignment> teamAssignmentList) {
        this.teamAssignmentList = teamAssignmentList;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public Collection<?> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(teamList);
        facts.addAll(dayList);
        facts.addAll(unavailabilityPenaltyList);
        // Do not add the planning entity's (teamAssignmentList) because that will be done automatically
        return facts;
    }

}
