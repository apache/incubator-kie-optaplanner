/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.meetingscheduling.domain;

public enum MeetingSchedulingConstraints {
    DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE("Do all meetings as soon as possible"),
    MINIMUM_TIMEGRAINS_BREAK("Minimum TimeGrains break between two consecutive meetings"),
    OVERLAPPING_MEETINGS("Overlapping meetings"),
    ASSIGN_LARGER_ROOMS_FIRST("Assign larger rooms first"),
    ROOM_STABILITY("Room Stability"),

    REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT("Required and preferred attendance conflict"),
    PREFERRED_ATTENDANCE_CONFLICT("Preferred attendance conflict"),

    ROOM_CONFLICT("Room conflict"),
    DONT_GO_IN_OVERTIME("Don't go in overtime"),
    REQUIRED_ATTENDANCE_CONFLICT("Required attendance conflict"),
    REQUIRED_ROOM_CAPACITY("Required room capacity"),
    START_AND_END_ON_SAME_DAY("Start and end on same day"),
    ENTIRE_GROUP_MEETING_NOT_SCHEDULED("Entire group meeting not scheduled");

    private final String constraintName;

    private MeetingSchedulingConstraints(String constraintName) {
        this.constraintName = constraintName;
    }

    public String toString() {
        return this.constraintName;
    }
}
