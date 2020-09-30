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

package org.optaplanner.examples.examination.solver.score;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.examination.domain.Exam;
import org.optaplanner.examples.examination.domain.Examination;
import org.optaplanner.examples.examination.domain.ExaminationConstraintConfiguration;
import org.optaplanner.examples.examination.domain.LeadingExam;
import org.optaplanner.examples.examination.domain.Period;
import org.optaplanner.examples.examination.domain.PeriodPenalty;
import org.optaplanner.examples.examination.domain.PeriodPenaltyType;
import org.optaplanner.examples.examination.domain.Room;
import org.optaplanner.examples.examination.domain.RoomPenalty;
import org.optaplanner.examples.examination.domain.RoomPenaltyType;
import org.optaplanner.examples.examination.domain.Student;
import org.optaplanner.examples.examination.domain.Topic;
import org.optaplanner.examples.examination.domain.solver.TopicConflict;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class ExaminationConstraintProviderTest {
    private final ConstraintVerifier<ExaminationConstraintProvider, Examination> constraintVerifier = ConstraintVerifier
            .build(new ExaminationConstraintProvider(), Examination.class, Exam.class);

    private final Student student1 = new Student(1L);
    private final Student student2 = new Student(2L);
    private final Student student3 = new Student(3L);
    private final Student student4 = new Student(4L);

    @Test
    public void conflictingExamsInSamePeriodTest() {
        Topic topic1 = new Topic();
        Topic topic2 = new Topic();
        TopicConflict conflict = new TopicConflict(topic1, topic2, 2);

        Period period = new Period();

        LeadingExam exam1 = new LeadingExam()
                .withTopic(topic1)
                .withPeriod(period)
                .withRoom(new Room());
        LeadingExam exam2 = new LeadingExam()
                .withTopic(topic2)
                .withPeriod(period)
                .withRoom(new Room());

        constraintVerifier.verifyThat(ExaminationConstraintProvider::conflictingExamsInSamePeriod)
                .given(conflict, exam1, exam2)
                .penalizesBy(2);
    }

    @Test
    public void periodDurationTooShortTest() {
        LeadingExam exam = new LeadingExam()
                .withTopic(new Topic().withDuration(2).withStudentList(Arrays.asList(student1, student2)))
                .withPeriod(new Period().withDuration(1))
                .withRoom(new Room());

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodDurationTooShort)
                .given(exam)
                .penalizesBy(2);
    }

    @Test
    public void roomCapacityTooSmallSingleLargeExamTest() {
        Period period = new Period();
        Room room = new Room().withCapacity(2);

        LeadingExam exam = new LeadingExam()
                .withTopic(new Topic().withStudentList(Arrays.asList(student1, student2, student3, student4)))
                .withPeriod(period)
                .withRoom(room);

        constraintVerifier.verifyThat(ExaminationConstraintProvider::roomCapacityTooSmall)
                .given(period, exam, room)
                .penalizesBy(2);
    }

    @Test
    public void roomCapacityTooSmallTwoExamsTest() {
        Period period = new Period();
        Room room = new Room().withCapacity(2);

        LeadingExam exam1 = new LeadingExam()
                .withTopic(new Topic().withStudentList(Arrays.asList(student1, student2)))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withTopic(new Topic().withStudentList(Arrays.asList(student3, student4)))
                .withPeriod(period)
                .withRoom(room);

        constraintVerifier.verifyThat(ExaminationConstraintProvider::roomCapacityTooSmall)
                .given(period, exam1, exam2, room)
                .penalizesBy(2);
    }

    @Test
    public void periodPenaltyTypeTest() {
        Topic topic1 = new Topic().withStudentList(Arrays.asList(student1, student2));
        Topic topic2 = new Topic().withStudentList(Arrays.asList(student1, student2));

        PeriodPenalty periodPenalty = new PeriodPenalty(topic1, topic2, PeriodPenaltyType.EXCLUSION);

        Period period = new Period().withPeriodIndex(1);

        LeadingExam exam1 = new LeadingExam()
                .withTopic(topic1)
                .withPeriod(period)
                .withRoom(new Room());
        LeadingExam exam2 = new LeadingExam()
                .withTopic(topic2)
                .withPeriod(period)
                .withRoom(new Room());

        // EXCLUSION
        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenaltyExclusion)
                .given(periodPenalty, exam1, exam2)
                .penalizesBy(4);

        // COINCIDENCE
        periodPenalty.setPeriodPenaltyType(PeriodPenaltyType.EXAM_COINCIDENCE);
        exam2.setPeriod(new Period().withPeriodIndex(0));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenaltyExamCoincidence)
                .given(periodPenalty, exam1, exam2)
                .penalizesBy(4);

        // AFTER
        // Exam2 after exam1.
        periodPenalty.setPeriodPenaltyType(PeriodPenaltyType.AFTER);
        exam2.setPeriod(new Period().withPeriodIndex(2));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenaltyAfter)
                .given(periodPenalty, exam1, exam2)
                .penalizesBy(4);

        // Exam2 before exam1. Should not trigger the AFTER constraint, since its not symmetrical.
        exam2.setPeriod(new Period().withPeriodIndex(0));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenaltyAfter)
                .given(periodPenalty, exam1, exam2)
                .penalizesBy(0);
    }

    @Test
    public void roomPenaltyExclusiveTest() {
        Topic topic1 = new Topic().withStudentList(Arrays.asList(student1, student2));
        Topic topic2 = new Topic().withStudentList(Arrays.asList(student3, student4));

        RoomPenalty penalty = new RoomPenalty().withTopic(topic1).withRoomPenaltyType(RoomPenaltyType.ROOM_EXCLUSIVE);

        Room room = new Room();
        Period period = new Period();

        LeadingExam exam1 = new LeadingExam().withPeriod(period).withRoom(room).withTopic(topic1);
        LeadingExam exam2 = new LeadingExam().withPeriod(period).withRoom(room).withTopic(topic2);

        constraintVerifier.verifyThat(ExaminationConstraintProvider::roomPenaltyExclusive)
                .given(penalty, exam1, exam2)
                .penalizesBy(4);
    }

    @Test
    public void twoExamsInARowAndInADayTest() {
        Topic topic1 = new Topic();
        Topic topic2 = new Topic();
        TopicConflict conflict = new TopicConflict(topic1, topic2, 2);

        LeadingExam exam1 = new LeadingExam()
                .withTopic(topic1)
                .withPeriod(new Period().withPeriodIndex(0).withDayIndex(0))
                .withRoom(new Room());
        LeadingExam exam2 = new LeadingExam()
                .withTopic(topic2)
                .withPeriod(new Period().withPeriodIndex(2).withDayIndex(1))
                .withRoom(new Room());

        // Not in a row nor day.
        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInARow)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);
        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInADay)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);

        // In neighboring indexes, but not the same day.
        exam2.setPeriod(new Period().withPeriodIndex(1).withDayIndex(1));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInARow)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);
        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInADay)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);

        // Not in a row on the same day.
        exam2.setPeriod(new Period().withPeriodIndex(2).withDayIndex(0));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInARow)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);
        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInADay)
                .given(conflict, exam1, exam2)
                .penalizesBy(2);

        // In a row on the same day. These two constraints don't overlap, therefore only the twoExamsInARow triggers.
        exam2.setPeriod(new Period().withPeriodIndex(1).withDayIndex(0));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInARow)
                .given(conflict, exam1, exam2)
                .penalizesBy(2);
        constraintVerifier.verifyThat(ExaminationConstraintProvider::twoExamsInADay)
                .given(conflict, exam1, exam2)
                .penalizesBy(0);
    }

    @Test
    public void periodSpreadTest() {
        ExaminationConstraintConfiguration config = new ExaminationConstraintConfiguration()
                // At least 1 period apart.
                .withPeriodSpreadLength(1);

        Topic topic1 = new Topic();
        Topic topic2 = new Topic();
        TopicConflict topicConflict = new TopicConflict(topic1, topic2, 3);

        LeadingExam exam1 = new LeadingExam()
                .withTopic(topic1)
                .withPeriod(new Period().withPeriodIndex(0))
                .withRoom(new Room());
        LeadingExam exam2 = new LeadingExam()
                .withTopic(topic2)
                .withPeriod(new Period().withPeriodIndex(0))
                .withRoom(new Room());

        // Period index saturation: 0, 0
        // Should trigger when period spread length is 1.
        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodSpread)
                .given(config, topicConflict, exam1, exam2)
                .penalizesBy(3);

        // Period index saturation: 0, 1
        // Should trigger when period spread length is 1.
        exam2.setPeriod(new Period().withPeriodIndex(1));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodSpread)
                .given(config, topicConflict, exam1, exam2)
                .penalizesBy(3);

        // Period index saturation: 0, 2
        exam2.setPeriod(new Period().withPeriodIndex(2));

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodSpread)
                .given(config, topicConflict, exam1, exam2)
                .penalizesBy(0);
    }

    @Test
    public void mixedDurations11Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 1
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2)
                .penalizesBy(0);
    }

    @Test
    public void mixedDurations12Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(2))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 2
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2)
                .penalizesBy(1);
    }

    @Test
    public void mixedDurations123Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(2))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 2, 3
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(2);
    }

    @Test
    public void mixedDurations113Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 1, 3
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(1);
    }

    @Test
    public void mixedDurations133Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 3, 3
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(1);
    }

    @Test
    public void mixedDurations131Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 1, 3, 1
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(1);
    }

    @Test
    public void mixedDurations431Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(4))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(3))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 4, 3, 1
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(2);
    }

    @Test
    public void mixedDurations411Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(4))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 4, 1, 1
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(1);
    }

    @Test
    public void mixedDurations441Test() {
        Period period = new Period();
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam()
                .withId(1L)
                .withTopic(new Topic()
                        .withId(1)
                        .withDuration(4))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam2 = new LeadingExam()
                .withId(2L)
                .withTopic(new Topic()
                        .withId(2)
                        .withDuration(4))
                .withPeriod(period)
                .withRoom(room);
        LeadingExam exam3 = new LeadingExam()
                .withId(3L)
                .withTopic(new Topic()
                        .withId(3)
                        .withDuration(1))
                .withPeriod(period)
                .withRoom(room);

        // Topic durations: 4, 4, 1
        constraintVerifier.verifyThat(ExaminationConstraintProvider::mixedDurations)
                .given(exam1, exam2, exam3)
                .penalizesBy(1);
    }

    @Test
    public void frontLoadTest() {
        LeadingExam exam = new LeadingExam()
                .withPeriod(new Period().withFrontLoadLast(true))
                .withTopic(new Topic().withFrontLoadLarge(true))
                .withRoom(new Room());

        constraintVerifier.verifyThat(ExaminationConstraintProvider::frontLoad)
                .given(exam)
                .penalizesBy(1);
    }

    @Test
    public void periodPenaltyTest() {
        Period period = new Period().withPenalty(5);
        Room room = new Room();
        LeadingExam exam1 = new LeadingExam().withPeriod(period).withRoom(room);

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenalty)
                .given(period, exam1)
                .penalizesBy(5);

        // Second exam in the same period. The penalty should be added for both of them.
        LeadingExam exam2 = new LeadingExam().withPeriod(period).withRoom(room);

        constraintVerifier.verifyThat(ExaminationConstraintProvider::periodPenalty)
                .given(period, exam1, exam2)
                .penalizesBy(10);
    }

    @Test
    public void roomPenaltyTest() {
        Room room = new Room().withPenalty(5);
        LeadingExam exam1 = new LeadingExam().withRoom(room).withPeriod(new Period());

        constraintVerifier.verifyThat(ExaminationConstraintProvider::roomPenalty)
                .given(room, exam1)
                .penalizesBy(5);

        // Second exam in the same period. The penalty should be added for both of them.
        LeadingExam exam2 = new LeadingExam().withRoom(room).withPeriod(new Period());

        constraintVerifier.verifyThat(ExaminationConstraintProvider::roomPenalty)
                .given(room, exam1, exam2)
                .penalizesBy(10);
    }
}
