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

package org.optaplanner.examples.examination.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningFactProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.examination.domain.solver.TopicConflict;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@PlanningSolution()
@XStreamAlias("Examination")
public class Examination extends AbstractPersistable {

    private InstitutionParametrization institutionParametrization;

    private List<Student> studentList;
    private List<Topic> topicList;
    private List<Period> periodList;
    private List<Room> roomList;

    private List<PeriodPenalty> periodPenaltyList;
    private List<RoomPenalty> roomPenaltyList;

    private List<Exam> examList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private HardSoftScore score;

    @PlanningFactProperty
    public InstitutionParametrization getInstitutionParametrization() {
        return institutionParametrization;
    }

    public void setInstitutionParametrization(InstitutionParametrization institutionParametrization) {
        this.institutionParametrization = institutionParametrization;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    @PlanningFactCollectionProperty
    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }

    @ValueRangeProvider(id = "periodRange")
    @PlanningFactCollectionProperty
    public List<Period> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<Period> periodList) {
        this.periodList = periodList;
    }

    @ValueRangeProvider(id = "roomRange")
    @PlanningFactCollectionProperty
    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @PlanningFactCollectionProperty
    public List<PeriodPenalty> getPeriodPenaltyList() {
        return periodPenaltyList;
    }

    public void setPeriodPenaltyList(List<PeriodPenalty> periodPenaltyList) {
        this.periodPenaltyList = periodPenaltyList;
    }

    @PlanningFactCollectionProperty
    public List<RoomPenalty> getRoomPenaltyList() {
        return roomPenaltyList;
    }

    public void setRoomPenaltyList(List<RoomPenalty> roomPenaltyList) {
        this.roomPenaltyList = roomPenaltyList;
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

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @PlanningFactCollectionProperty
    private List<TopicConflict> getTopicConflictList() {
        List<TopicConflict> topicConflictList = new ArrayList<TopicConflict>();
        for (Topic leftTopic : topicList) {
            for (Topic rightTopic : topicList) {
                if (leftTopic.getId() < rightTopic.getId()) {
                    int studentSize = 0;
                    for (Student student : leftTopic.getStudentList()) {
                        if (rightTopic.getStudentList().contains(student)) {
                            studentSize++;
                        }
                    }
                    if (studentSize > 0) {
                        topicConflictList.add(new TopicConflict(leftTopic, rightTopic, studentSize));
                    }
                }
            }
        }
        return topicConflictList;
    }

}
