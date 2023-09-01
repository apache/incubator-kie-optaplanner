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
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.score.TestdataBendableScoreSolution;

class BendableScoreInlinerTest extends AbstractScoreInlinerTest<TestdataBendableScoreSolution, BendableScore> {

    @Test
    void defaultScore() {
        BendableScoreInliner scoreInliner =
                new BendableScoreInliner(constraintMatchEnabled, 1, 2);
        assertThat(scoreInliner.extractScore(0)).isEqualTo(buildScore(0, 0, 0));
    }

    @Test
    void impactHard() {
        BendableScoreInliner scoreInliner = new BendableScoreInliner(constraintMatchEnabled, 1, 2);

        BendableScore constraintWeight = buildScore(90, 0, 0);
        WeightedScoreImpacter<BendableScore, BendableScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(90, 0, 0));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(270, 0, 0));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(90, 0, 0));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 0));
    }

    @Test
    void impactSoft1() {
        BendableScoreInliner scoreInliner = new BendableScoreInliner(constraintMatchEnabled, 1, 2);

        BendableScore constraintWeight = buildScore(0, 90, 0);
        WeightedScoreImpacter<BendableScore, BendableScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 90, 0));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 270, 0));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 90, 0));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 0));
    }

    @Test
    void impactSoft2() {
        BendableScoreInliner scoreInliner = new BendableScoreInliner(constraintMatchEnabled, 1, 2);

        BendableScore constraintWeight = buildScore(0, 0, 90);
        WeightedScoreImpacter<BendableScore, BendableScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(1, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 90));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(2, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 270));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 90));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 0));
    }

    @Test
    void impactAll() {
        BendableScoreInliner scoreInliner = new BendableScoreInliner(constraintMatchEnabled, 1, 2);

        BendableScore constraintWeight = buildScore(10, 100, 1_000);
        WeightedScoreImpacter<BendableScore, BendableScoreContext> hardImpacter =
                scoreInliner.buildWeightedScoreImpacter(buildConstraint(constraintWeight), constraintWeight);
        UndoScoreImpacter undo1 = hardImpacter.impactScore(10, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(100, 1_000, 10_000));

        UndoScoreImpacter undo2 = hardImpacter.impactScore(20, JustificationsSupplier.empty());
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(300, 3_000, 30_000));

        undo2.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(100, 1_000, 10_000));

        undo1.run();
        assertThat(scoreInliner.extractScore(0))
                .isEqualTo(buildScore(0, 0, 0));
    }

    @Override
    protected SolutionDescriptor<TestdataBendableScoreSolution> buildSolutionDescriptor() {
        return TestdataBendableScoreSolution.buildSolutionDescriptor();
    }

    private BendableScore buildScore(int hard, int soft1, int soft2) {
        return BendableScore.of(
                new int[] { hard },
                new int[] { soft1, soft2 });
    }

}
