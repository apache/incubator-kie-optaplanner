/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.dinnerparty.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.*;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

import java.util.EnumSet;
import java.util.List;

@PlanningSolution
@XStreamAlias("DinnerParty")
public class DinnerParty extends AbstractPersistable {

    @PlanningFactCollectionProperty
    private List<Job> jobList;
    @PlanningFactCollectionProperty
    private List<Guest> guestList;
    @PlanningFactCollectionProperty
    private List<HobbyPractician> hobbyPracticianList;
    @PlanningFactCollectionProperty
    private List<Table> tableList;
    @PlanningFactCollectionProperty
    private List<Seat> seatList;

    private List<SeatDesignation> seatDesignationList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {SimpleScoreDefinition.class})
    private SimpleScore score;

    @PlanningFactCollectionProperty
    public EnumSet<Hobby> getHobbyType() {
        return EnumSet.allOf(Hobby.class);
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    @PlanningFactCollectionProperty
    public EnumSet<JobType> getJobType() {
        return EnumSet.allOf(JobType.class);
    }

    public List<Guest> getGuestList() {
        return guestList;
    }

    public void setGuestList(List<Guest> guestList) {
        this.guestList = guestList;
    }

    public List<HobbyPractician> getHobbyPracticianList() {
        return hobbyPracticianList;
    }

    public void setHobbyPracticianList(List<HobbyPractician> hobbyPracticianList) {
        this.hobbyPracticianList = hobbyPracticianList;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public void setTableList(List<Table> tableList) {
        this.tableList = tableList;
    }

    @ValueRangeProvider(id = "seatRange")
    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    @PlanningEntityCollectionProperty
    public List<SeatDesignation> getSeatDesignationList() {
        return seatDesignationList;
    }

    public void setSeatDesignationList(List<SeatDesignation> seatDesignationList) {
        this.seatDesignationList = seatDesignationList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
