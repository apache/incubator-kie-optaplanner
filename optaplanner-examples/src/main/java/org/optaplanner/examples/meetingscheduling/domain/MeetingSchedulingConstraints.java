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
    ROOM_CONFLICT("Room conflict", 1),
    DONT_GO_IN_OVERTIME("Don't go in overtime", 1),
    REQUIRED_ATTENDANCE_CONFLICT("Required attendance conflict", 1),
    REQUIRED_ROOM_CAPACITY("Required room capacity", 1),
    START_AND_END_ON_SAME_DAY("Start and end on same day", 1),
    ENTIRE_GROUP_MEETING_NOT_SCHEDULED("Entire group meeting not scheduled", 1),

    REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT("Required and preferred attendance conflict", 1),
    PREFERRED_ATTENDANCE_CONFLICT("Preferred attendance conflict", 1),

    DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE("Do all meetings as soon as possible", 1),
    MINIMUM_TIMEGRAINS_BREAK("Minimum TimeGrains break between two consecutive meetings", 100),
    OVERLAPPING_MEETINGS("Overlapping meetings", 10),
    ASSIGN_LARGER_ROOMS_FIRST("Assign larger rooms first", 1),
    ROOM_STABILITY("Room Stability", 1);

    private final String constraintName;
    private int constraintWeight;

    private MeetingSchedulingConstraints(String constraintName, int constraintWeight) {
        this.constraintName = constraintName;
        this.constraintWeight = constraintWeight;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public int getConstraintWeight() {
        return this.constraintWeight;
    }

    public void setConstraintWeight(int constraintWeight) {
        this.constraintWeight = constraintWeight;
    }

    public String toString() {
        return this.constraintName;
    }
}
