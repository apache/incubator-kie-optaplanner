/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.meetingscheduling.optional.score;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.meetingscheduling.domain.*;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class MeetingSchedulingConstraintProviderTest {

    private final ConstraintVerifier<MeetingSchedulingConstraintProvider, MeetingSchedule> constraintVerifier =
            ConstraintVerifier.build(new MeetingSchedulingConstraintProvider(), MeetingSchedule.class,
                    MeetingAssignment.class);

    @Test
    public void roomConflictUnpenalized() {
        Room room = new Room();

        TimeGrain timeGrain1 = new TimeGrain();
        timeGrain1.setGrainIndex(0);

        Meeting meeting1 = new Meeting();
        meeting1.setDurationInGrains(4);

        MeetingAssignment leftAssignment = new MeetingAssignment(meeting1, timeGrain1, room);
        leftAssignment.setId(0L);

        TimeGrain timeGrain2 = new TimeGrain();
        timeGrain2.setGrainIndex(4);

        Meeting meeting2 = new Meeting();
        meeting2.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(meeting2, timeGrain2, room);
        rightAssignment.setId(1L);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void roomConflictPenalized() {
        Room room = new Room();

        TimeGrain timeGrain1 = new TimeGrain();
        timeGrain1.setGrainIndex(0);

        Meeting meeting1 = new Meeting();
        meeting1.setDurationInGrains(4);

        MeetingAssignment leftAssignment = new MeetingAssignment(meeting1, timeGrain1, room);
        leftAssignment.setId(0L);

        TimeGrain timeGrain2 = new TimeGrain();
        timeGrain2.setGrainIndex(2);

        Meeting meeting2 = new Meeting();
        meeting2.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(meeting2, timeGrain2, room);
        rightAssignment.setId(1L);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomConflict)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    public void avoidOvertimeUnpenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);

        TimeGrain assignmentTimeGrain = new TimeGrain();
        assignmentTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, assignmentTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(0);
    }

    @Test
    public void avoidOvertimePenalized() {
        TimeGrain assignmentTimeGrain = new TimeGrain();
        assignmentTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, assignmentTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::avoidOvertime)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    public void requiredAttendanceConflictUnpenalized() {
        Person person = new Person();
        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance1 = new RequiredAttendance();
        requiredAttendance1.setId(0L);
        requiredAttendance1.setPerson(person);
        requiredAttendance1.setMeeting(leftMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance2 = new RequiredAttendance();
        requiredAttendance2.setId(1L);
        requiredAttendance2.setPerson(person);
        requiredAttendance2.setMeeting(rightMeeting);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(requiredAttendance1, requiredAttendance2, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void requiredAttendanceConflictPenalized() {
        Person person = new Person();
        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance1 = new RequiredAttendance();
        requiredAttendance1.setId(0L);
        requiredAttendance1.setPerson(person);
        requiredAttendance1.setMeeting(leftMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance2 = new RequiredAttendance();
        requiredAttendance2.setId(1L);
        requiredAttendance2.setPerson(person);
        requiredAttendance2.setMeeting(rightMeeting);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(2);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAttendanceConflict)
                .given(requiredAttendance1, requiredAttendance2, leftAssignment, rightAssignment)
                .penalizesBy(2);
    }

    @Test
    public void requiredRoomCapacityUnpenalized() {
        Room room = new Room();
        room.setCapacity(2);

        List<RequiredAttendance> requiredAttendanceList = new ArrayList<>(1);
        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendanceList.add(requiredAttendance);

        List<PreferredAttendance> preferredAttendanceList = new ArrayList<>(1);
        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendanceList.add(preferredAttendance);

        Meeting meeting = new Meeting();
        meeting.setRequiredAttendanceList(requiredAttendanceList);
        meeting.setPreferredAttendanceList(preferredAttendanceList);

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    public void requiredRoomCapacityPenalized() {
        Room room = new Room();
        room.setCapacity(1);

        List<RequiredAttendance> requiredAttendanceList = new ArrayList<>(1);
        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendanceList.add(requiredAttendance);

        List<PreferredAttendance> preferredAttendanceList = new ArrayList<>(1);
        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendanceList.add(preferredAttendance);

        Meeting meeting = new Meeting();
        meeting.setRequiredAttendanceList(requiredAttendanceList);
        meeting.setPreferredAttendanceList(preferredAttendanceList);

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredRoomCapacity)
                .given(meetingAssignment)
                .penalizesBy(1);
    }

    @Test
    public void startAndEndOnSameDayUnpenalized() {
        Day day = new Day();
        day.setDayOfYear(0);

        TimeGrain startingTimeGrain = new TimeGrain();
        startingTimeGrain.setGrainIndex(0);
        startingTimeGrain.setDay(day);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, room);

        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);
        timeGrain.setDay(day);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(0);
    }

    @Test
    public void startAndEndOnSameDayPenalized() {
        Day day = new Day();
        day.setDayOfYear(0);

        TimeGrain startingTimeGrain = new TimeGrain();
        startingTimeGrain.setGrainIndex(0);
        startingTimeGrain.setDay(day);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, room);

        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(3);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::startAndEndOnSameDay)
                .given(meetingAssignment, timeGrain)
                .penalizesBy(1);
    }

    @Test
    public void requiredAndPreferredAttendanceConflictUnpenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendance.setPerson(person);
        requiredAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendance.setPerson(person);
        preferredAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void requiredAndPreferredAttendanceConflictPenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance requiredAttendance = new RequiredAttendance();
        requiredAttendance.setPerson(person);
        requiredAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance preferredAttendance = new PreferredAttendance();
        preferredAttendance.setPerson(person);
        preferredAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceConflict)
                .given(requiredAttendance, preferredAttendance, leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    public void preferredAttendanceConflictUnpenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        PreferredAttendance leftAttendance = new PreferredAttendance();
        leftAttendance.setId(0L);
        leftAttendance.setPerson(person);
        leftAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance rightAttendance = new PreferredAttendance();
        rightAttendance.setId(1L);
        rightAttendance.setPerson(person);
        rightAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void preferredAttendanceConflictPenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        PreferredAttendance leftAttendance = new PreferredAttendance();
        leftAttendance.setId(0L);
        leftAttendance.setPerson(person);
        leftAttendance.setMeeting(leftMeeting);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        PreferredAttendance rightAttendance = new PreferredAttendance();
        rightAttendance.setId(1L);
        rightAttendance.setPerson(person);
        rightAttendance.setMeeting(rightMeeting);

        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::preferredAttendanceConflict)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    public void doMeetingsAsSoonAsPossibleUnpenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(1);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, timeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    public void doMeetingsAsSoonAsPossiblePenalized() {
        TimeGrain timeGrain = new TimeGrain();
        timeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, timeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::doMeetingsAsSoonAsPossible)
                .given(meetingAssignment)
                .penalizesBy(3);
    }

    @Test
    public void oneBreakBetweenConsecutiveMeetingsUnpenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(meeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        MeetingAssignment rightAssignment = new MeetingAssignment(meeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void oneBreakBetweenConsecutiveMeetingsPenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting meeting = new Meeting();
        meeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(meeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(meeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::oneBreakBetweenConsecutiveMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    public void overlappingMeetingsUnpenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting leftMeeting = new Meeting();
        leftMeeting.setId(1L);
        leftMeeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(4);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setId(0L);
        rightMeeting.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void overlappingMeetingsPenalized() {
        TimeGrain leftTimeGrain = new TimeGrain();
        leftTimeGrain.setGrainIndex(0);

        Meeting leftMeeting = new Meeting();
        leftMeeting.setId(1L);
        leftMeeting.setDurationInGrains(4);

        Room room = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftTimeGrain, room);

        TimeGrain rightTimeGrain = new TimeGrain();
        rightTimeGrain.setGrainIndex(0);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setId(0L);
        rightMeeting.setDurationInGrains(4);

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightTimeGrain, room);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::overlappingMeetings)
                .given(leftAssignment, rightAssignment)
                .penalizesBy(1);
    }

    @Test
    public void assignLargerRoomsFirstUnpenalized() {
        Room meetingRoom = new Room();
        meetingRoom.setCapacity(1);

        Meeting meeting = new Meeting();

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, meetingRoom);
        meetingAssignment.setRoom(meetingRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment)
                .penalizesBy(0);
    }

    @Test
    public void assignLargerRoomsFirstPenalized() {
        Room meetingRoom = new Room();
        meetingRoom.setCapacity(1);

        Meeting meeting = new Meeting();

        TimeGrain startingTimeGrain = new TimeGrain();

        MeetingAssignment meetingAssignment = new MeetingAssignment(meeting, startingTimeGrain, meetingRoom);

        Room largerRoom = new Room();
        largerRoom.setCapacity(2);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::assignLargerRoomsFirst)
                .given(meetingAssignment, largerRoom)
                .penalizesBy(1);
    }

    @Test
    public void roomStabilityUnpenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance leftAttendance = new RequiredAttendance();
        leftAttendance.setMeeting(leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance rightAttendance = new RequiredAttendance();
        rightAttendance.setMeeting(rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftStartTimeGrain = new TimeGrain();
        leftStartTimeGrain.setGrainIndex(0);

        Room leftRoom = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftStartTimeGrain, leftRoom);

        TimeGrain rightStartTimeGrain = new TimeGrain();
        rightStartTimeGrain.setGrainIndex(8);

        Room rightRoom = new Room();

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightStartTimeGrain, rightRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(0);
    }

    @Test
    public void roomStabilityPenalized() {
        Person person = new Person();

        Meeting leftMeeting = new Meeting();
        leftMeeting.setDurationInGrains(4);

        RequiredAttendance leftAttendance = new RequiredAttendance();
        leftAttendance.setMeeting(leftMeeting);
        leftAttendance.setPerson(person);

        Meeting rightMeeting = new Meeting();
        rightMeeting.setDurationInGrains(4);

        RequiredAttendance rightAttendance = new RequiredAttendance();
        rightAttendance.setMeeting(rightMeeting);
        rightAttendance.setPerson(person);

        TimeGrain leftStartTimeGrain = new TimeGrain();
        leftStartTimeGrain.setGrainIndex(0);

        Room leftRoom = new Room();

        MeetingAssignment leftAssignment = new MeetingAssignment(leftMeeting, leftStartTimeGrain, leftRoom);

        TimeGrain rightStartTimeGrain = new TimeGrain();
        rightStartTimeGrain.setGrainIndex(4);

        Room rightRoom = new Room();

        MeetingAssignment rightAssignment = new MeetingAssignment(rightMeeting, rightStartTimeGrain, rightRoom);

        constraintVerifier.verifyThat(MeetingSchedulingConstraintProvider::roomStability)
                .given(leftAttendance, rightAttendance, leftAssignment, rightAssignment)
                .penalizesBy(1);
    }
}
