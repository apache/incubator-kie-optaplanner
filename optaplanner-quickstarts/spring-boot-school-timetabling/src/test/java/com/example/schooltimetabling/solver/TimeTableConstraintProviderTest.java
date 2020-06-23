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

package com.example.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.example.schooltimetabling.domain.Lesson;
import com.example.schooltimetabling.domain.Room;
import com.example.schooltimetabling.domain.TimeTable;
import com.example.schooltimetabling.domain.Timeslot;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TimeTableConstraintProviderTest {

    @Autowired
    private ConstraintVerifier<TimeTableConstraintProvider, TimeTable> constraintVerifier;

    @Test
    public void roomConflict() {
        Room roomA = new Room("Room A");
        Timeslot monday1 = new Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30));

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(
                        new Lesson("Math", "A. Turing", "9th grade", monday1, roomA),
                        new Lesson("Chemistry", "M. Curie", "10th grade", monday1, roomA)
                )
                .penalizesBy(1);
    }

    // TODO other constraints

}
