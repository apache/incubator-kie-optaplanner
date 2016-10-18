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

package org.optaplanner.core.api.score.buildin;

import org.optaplanner.core.api.score.FeasibilityScore;
import org.optaplanner.core.api.score.Score;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractScoreTest {

    public static void assertScoresEqualsAndHashCode(Score... scores) {
        for (int i = 0; i < scores.length; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                assertThat(scores[j]).isEqualTo(scores[i]);
                assertThat(scores[j].hashCode()).isEqualTo(scores[i].hashCode());
                assertThat(scores[i].compareTo(scores[j])).isEqualTo(0);
            }
        }
    }

    public static void assertScoresNotEquals(Score... scores) {
        for (int i = 0; i < scores.length; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                assertThat(scores[j]).isNotEqualTo(scores[i]);
                assertThat(scores[i].compareTo(scores[j])).isNotEqualTo(0);
            }
        }
    }

    public static void assertScoreNotFeasible(FeasibilityScore... scores) {
        for (FeasibilityScore score : scores) {
            assertThat(score.isFeasible()).as(score + " should not be feasible.").isFalse();
        }
    }

    public static void assertScoreFeasible(FeasibilityScore ... scores) {
        for (FeasibilityScore score : scores) {
            assertThat(score.isFeasible()).as(score + " should be feasible.").isTrue();
        }
    }

}
