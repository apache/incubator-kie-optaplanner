/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.examination.domain;

import java.util.List;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.examination.domain.solver.TopicConflict;

@PlanningSolution
public class Examination extends AbstractPersistable {

    private ExaminationConstraintConfiguration constraintConfiguration;

    private List<Student> studentList;
    private List<Topic> topicList;
    private List<Period> periodList;
    private List<Room> roomList;

    private List<PeriodPenalty> periodPenaltyList;
    private List<RoomPenalty> roomPenaltyList;
    private List<TopicConflict> topicConflictList;

    private List<Exam> examList;

    private HardSoftScore score;

    public Examination() {
    }

    public Examination(long id) {
        super(id);
    }

    @ConstraintConfigurationProvider
    public ExaminationConstraintConfiguration getConstraintConfiguration() {
        return constraintConfiguration;
    }

    public void setConstraintConfiguration(ExaminationConstraintConfiguration constraintConfiguration) {
        this.constraintConfiguration = constraintConfiguration;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    @ProblemFactCollectionProperty
    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    public List<Period> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<Period> periodList) {
        this.periodList = periodList;
    }

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @ProblemFactCollectionProperty
    public List<PeriodPenalty> getPeriodPenaltyList() {
        return periodPenaltyList;
    }

    public void setPeriodPenaltyList(List<PeriodPenalty> periodPenaltyList) {
        this.periodPenaltyList = periodPenaltyList;
    }

    @ProblemFactCollectionProperty
    public List<RoomPenalty> getRoomPenaltyList() {
        return roomPenaltyList;
    }

    public void setRoomPenaltyList(List<RoomPenalty> roomPenaltyList) {
        this.roomPenaltyList = roomPenaltyList;
    }

    @ProblemFactCollectionProperty
    public List<TopicConflict> getTopicConflictList() {
        return topicConflictList;
    }

    public void setTopicConflictList(List<TopicConflict> topicConflictList) {
        this.topicConflictList = topicConflictList;
    }

    @PlanningEntityCollectionProperty
    public List<Exam> getExamList() {
        return examList;
    }

    public void setExamList(List<Exam> examList) {
        this.examList = examList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

}
