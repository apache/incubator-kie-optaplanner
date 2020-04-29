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

package org.optaplanner.core.api.score.stream;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.score.TestdataSimpleBigDecimalScoreSolution;
import org.optaplanner.core.impl.testdata.domain.score.TestdataSimpleLongScoreSolution;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishEntity;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishSolution;
import org.optaplanner.core.impl.testdata.domain.score.lavish.TestdataLavishValue;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

public class ScoringConstraintStreamTest extends AbstractConstraintStreamTest {

    public ScoringConstraintStreamTest(boolean constraintMatchEnabled, ConstraintStreamImplType constraintStreamImplType) {
        super(constraintMatchEnabled, constraintStreamImplType);
    }

    @Test
    public void penalizeUniUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-7));
    }

    @Test
    public void penalizeUni() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE, entity -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-14));
    }

    @Test
    public void penalizeUniLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .penalizeLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, entity -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-14));
    }

    @Test
    public void penalizeUniBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .penalizeBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE, entity -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-14)));
    }

    @Test
    public void rewardUniUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(7));
    }

    @Test
    public void rewardUni() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE, entity -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(14));
    }

    @Test
    public void rewardUniLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .rewardLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, entity -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(14));
    }

    @Test
    public void rewardUniBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .rewardBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE, entity -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(14)));
    }

    @Test
    public void impactPositiveUniUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(7));
    }

    @Test
    public void impactPositiveUni() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, entity -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(14));
    }

    @Test
    public void impactPositiveUniLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, entity -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(14));
    }

    @Test
    public void impactPositiveUniBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE, entity -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(14)));
    }

    @Test
    public void impactNegativeUni() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector =
                buildScoreDirector(factory -> factory.from(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, entity -> -2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-14));
    }

    @Test
    public void impactNegativeUniLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, entity -> -2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-14));
    }

    @Test
    public void impactNegativeUniBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.from(TestdataEntity.class)
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE, entity -> BigDecimal.valueOf(-2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-14)));
    }

    @Test
    public void penalizeBiUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-21));
    }

    @Test
    public void penalizeBi() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-42));
    }

    @Test
    public void penalizeBiLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .penalizeLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-42));
    }

    @Test
    public void penalizeBiBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .penalizeBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-42)));
    }

    @Test
    public void rewardBiUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(21));
    }

    @Test
    public void rewardBi() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(42));
    }

    @Test
    public void rewardBiLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .rewardLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(42));
    }

    @Test
    public void rewardBiBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .rewardBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(42)));
    }

    @Test
    public void impactPositiveBiUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(21));
    }

    @Test
    public void impactPositiveBi() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(42));
    }

    @Test
    public void impactPositiveBiLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(42));
    }

    @Test
    public void impactPositiveBiBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(42)));
    }

    @Test
    public void impactNegativeBi() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class)
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2) -> -2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-42));
    }

    @Test
    public void impactNegativeBiLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2) -> -2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-42));
    }

    @Test
    public void impactNegativeBiBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class)
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2) -> BigDecimal.valueOf(-2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-42)));
    }

    @Test
    public void penalizeTriUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-2));
    }

    @Test
    public void penalizeTri() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-4));
    }

    @Test
    public void penalizeTriLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .penalizeLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-4));
    }

    @Test
    public void penalizeTriBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .penalizeBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-4)));
    }

    @Test
    public void rewardTriUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(2));
    }

    @Test
    public void rewardTri() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(4));
    }

    @Test
    public void rewardTriLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .rewardLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(4));
    }

    @Test
    public void rewardTriBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .rewardBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(4)));
    }

    @Test
    public void impactPositiveTriUnweighed() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(2));
    }

    @Test
    public void impactPositiveTri() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(4));
    }

    @Test
    public void impactPositiveTriLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(4));
    }

    @Test
    public void impactPositiveTriBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(4)));
    }

    @Test
    public void impactNegativeTri() {
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value) -> -2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-4));
    }

    @Test
    public void impactNegativeTriLong() {
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value) -> -2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-4));
    }

    @Test
    public void impactNegativeTriBigDecimal() {
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value) -> BigDecimal.valueOf(-2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-4)));
    }

    @Test
    public void penalizeQuadUnweighed() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-2));
    }

    @Test
    public void penalizeQuad() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .penalize(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value, value2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-4));
    }

    @Test
    public void penalizeQuadLong() {
        assumeDrools();
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .penalizeLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value, value2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-4));
    }

    @Test
    public void penalizeQuadBigDecimal() {
        assumeDrools();
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .penalizeBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value, value2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-4)));
    }

    @Test
    public void rewardQuadUnweighed() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(2));
    }

    @Test
    public void rewardQuad() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .reward(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value, value2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(4));
    }

    @Test
    public void rewardQuadLong() {
        assumeDrools();
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .rewardLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value, value2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(4));
    }

    @Test
    public void rewardQuadBigDecimal() {
        assumeDrools();
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .rewardBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value, value2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(4)));
    }

    @Test
    public void impactPositiveQuadUnweighed() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(2));
    }

    @Test
    public void impactPositiveQuad() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value, value2) -> 2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(4));
    }

    @Test
    public void impactPositiveQuadLong() {
        assumeDrools();
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value, value2) -> 2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(4));
    }

    @Test
    public void impactPositiveQuadBigDecimal() {
        assumeDrools();
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value, value2) -> BigDecimal.valueOf(2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(4)));
    }

    @Test
    public void impactNegativeQuad() {
        assumeDrools();
        TestdataLavishSolution solution = TestdataLavishSolution.generateSolution();

        InnerScoreDirector<TestdataLavishSolution> scoreDirector = buildScoreDirector(
                factory -> factory.fromUniquePair(TestdataLavishEntity.class, equal(TestdataLavishEntity::getValue))
                        .join(TestdataLavishValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataLavishValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impact(TEST_CONSTRAINT_NAME, SimpleScore.ONE, (entity, entity2, value, value2) -> -2));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleScore.of(-4));
    }

    @Test
    public void impactNegativeQuadLong() {
        assumeDrools();
        TestdataSimpleLongScoreSolution solution = TestdataSimpleLongScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleLongScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleLongScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impactLong(TEST_CONSTRAINT_NAME, SimpleLongScore.ONE, (entity, entity2, value, value2) -> -2L));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleLongScore.of(-4));
    }

    @Test
    public void impactNegativeQuadBigDecimal() {
        assumeDrools();
        TestdataSimpleBigDecimalScoreSolution solution = TestdataSimpleBigDecimalScoreSolution.generateSolution();

        InnerScoreDirector<TestdataSimpleBigDecimalScoreSolution> scoreDirector = buildScoreDirector(
                TestdataSimpleBigDecimalScoreSolution::buildSolutionDescriptor,
                factory -> factory.fromUniquePair(TestdataEntity.class, equal(TestdataEntity::getValue))
                        .join(TestdataValue.class, equal((entity, entity2) -> entity.getValue(), identity()))
                        .join(TestdataValue.class, equal((entity, entity2, value) -> value, identity()))
                        .impactBigDecimal(TEST_CONSTRAINT_NAME, SimpleBigDecimalScore.ONE,
                                (entity, entity2, value, value2) -> BigDecimal.valueOf(-2)));

        scoreDirector.setWorkingSolution(solution);
        scoreDirector.calculateScore();
        assertThat(scoreDirector.calculateScore()).isEqualTo(SimpleBigDecimalScore.of(BigDecimal.valueOf(-4)));
    }

}
