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

package org.optaplanner.constraint.streams.common.inliner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.score.TestdataHardMediumSoftScoreSolution;

class HardMediumSoftScoreInlinerTest
        extends AbstractScoreInlinerTest<TestdataHardMediumSoftScoreSolution, HardMediumSoftScore> {

    @Test
    void defaultScore() {
        HardMediumSoftScoreInliner scoreInliner =
                new HardMediumSoftScoreInliner(constraintMatchEnabled);
        assertThat(scoreInliner.extractScore(0)).isEqualTo(HardMediumSoftScore.ZERO);
    }

    @Test
    void impactHard() {
        HardMediumSoftScoreInliner scoreInliner =
                new HardMediumSoftScoreInliner(constraintMatchEnabled);

        HardMediumSoftScore constraintWeight = HardMediumSoftScore.ofHard(90);
        WeightedScoreImpacter<HardMediumSoftScore, HardMediumSoftScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(90, 0, 0));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(270, 0, 0));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(90, 0, 0));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 0));
    }

    @Test
    void impactMedium() {
        HardMediumSoftScoreInliner scoreInliner =
                new HardMediumSoftScoreInliner(constraintMatchEnabled);

        HardMediumSoftScore constraintWeight = HardMediumSoftScore.ofMedium(90);
        WeightedScoreImpacter<HardMediumSoftScore, HardMediumSoftScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 90, 0));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 270, 0));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 90, 0));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 0));
    }

    @Test
    void impactSoft() {
        HardMediumSoftScoreInliner scoreInliner =
                new HardMediumSoftScoreInliner(constraintMatchEnabled);

        HardMediumSoftScore constraintWeight = HardMediumSoftScore.ofSoft(90);
        WeightedScoreImpacter<HardMediumSoftScore, HardMediumSoftScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 90));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 270));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 90));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 0));
    }

    @Test
    void impactAll() {
        HardMediumSoftScoreInliner scoreInliner =
                new HardMediumSoftScoreInliner(constraintMatchEnabled);

        HardMediumSoftScore constraintWeight = HardMediumSoftScore.of(10, 100, 1_000);
        WeightedScoreImpacter<HardMediumSoftScore, HardMediumSoftScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(10, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(100, 1_000, 10_000));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(20, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(300, 3_000, 30_000));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(100, 1_000, 10_000));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(HardMediumSoftScore.of(0, 0, 0));
    }

    @Override
    protected SolutionDescriptor<TestdataHardMediumSoftScoreSolution> buildSolutionDescriptor() {
        return TestdataHardMediumSoftScoreSolution.buildSolutionDescriptor();
    }
}
