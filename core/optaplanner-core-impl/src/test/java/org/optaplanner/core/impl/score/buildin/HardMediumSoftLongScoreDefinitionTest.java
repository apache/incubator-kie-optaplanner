package org.optaplanner.core.impl.score.buildin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

class HardMediumSoftLongScoreDefinitionTest {

    @Test
    void getZeroScore() {
        HardMediumSoftLongScore score = new HardMediumSoftLongScoreDefinition().getZeroScore();
        assertThat(score).isEqualTo(HardMediumSoftLongScore.ZERO);
    }

    @Test
    void getSoftestOneScore() {
        HardMediumSoftLongScore score = new HardMediumSoftLongScoreDefinition().getOneSoftestScore();
        assertThat(score).isEqualTo(HardMediumSoftLongScore.ONE_SOFT);
    }

    @Test
    void getLevelsSize() {
        assertThat(new HardMediumSoftLongScoreDefinition().getLevelsSize()).isEqualTo(3);
    }

    @Test
    void getLevelLabels() {
        assertThat(new HardMediumSoftLongScoreDefinition().getLevelLabels())
                .containsExactly("hard score", "medium score", "soft score");
    }

    @Test
    void getFeasibleLevelsSize() {
        assertThat(new HardMediumSoftLongScoreDefinition().getFeasibleLevelsSize()).isEqualTo(1);
    }

    @Test
    void buildOptimisticBoundOnlyUp() {
        HardMediumSoftLongScoreDefinition scoreDefinition = new HardMediumSoftLongScoreDefinition();
        HardMediumSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 3),
                HardMediumSoftLongScore.of(-1L, -2L, -3L));
        assertThat(optimisticBound.initScore()).isEqualTo(0);
        assertThat(optimisticBound.hardScore()).isEqualTo(Long.MAX_VALUE);
        assertThat(optimisticBound.mediumScore()).isEqualTo(Long.MAX_VALUE);
        assertThat(optimisticBound.softScore()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void buildOptimisticBoundOnlyDown() {
        HardMediumSoftLongScoreDefinition scoreDefinition = new HardMediumSoftLongScoreDefinition();
        HardMediumSoftLongScore optimisticBound = scoreDefinition.buildOptimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 3),
                HardMediumSoftLongScore.of(-1L, -2L, -3L));
        assertThat(optimisticBound.initScore()).isEqualTo(0);
        assertThat(optimisticBound.hardScore()).isEqualTo(-1L);
        assertThat(optimisticBound.mediumScore()).isEqualTo(-2L);
        assertThat(optimisticBound.softScore()).isEqualTo(-3L);
    }

    @Test
    void buildPessimisticBoundOnlyUp() {
        HardMediumSoftLongScoreDefinition scoreDefinition = new HardMediumSoftLongScoreDefinition();
        HardMediumSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_UP, 3),
                HardMediumSoftLongScore.of(-1L, -2L, -3L));
        assertThat(pessimisticBound.initScore()).isEqualTo(0);
        assertThat(pessimisticBound.hardScore()).isEqualTo(-1L);
        assertThat(pessimisticBound.mediumScore()).isEqualTo(-2L);
        assertThat(pessimisticBound.softScore()).isEqualTo(-3L);
    }

    @Test
    void buildPessimisticBoundOnlyDown() {
        HardMediumSoftLongScoreDefinition scoreDefinition = new HardMediumSoftLongScoreDefinition();
        HardMediumSoftLongScore pessimisticBound = scoreDefinition.buildPessimisticBound(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 3),
                HardMediumSoftLongScore.of(-1L, -2L, -3L));
        assertThat(pessimisticBound.initScore()).isEqualTo(0);
        assertThat(pessimisticBound.hardScore()).isEqualTo(Long.MIN_VALUE);
        assertThat(pessimisticBound.mediumScore()).isEqualTo(Long.MIN_VALUE);
        assertThat(pessimisticBound.softScore()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void divideBySanitizedDivisor() {
        HardMediumSoftLongScoreDefinition scoreDefinition = new HardMediumSoftLongScoreDefinition();
        HardMediumSoftLongScore dividend = scoreDefinition.fromLevelNumbers(2, new Number[] { 0L, 1L, 10L });
        HardMediumSoftLongScore zeroDivisor = scoreDefinition.getZeroScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, zeroDivisor))
                .isEqualTo(dividend);
        HardMediumSoftLongScore oneDivisor = scoreDefinition.getOneSoftestScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, oneDivisor))
                .isEqualTo(dividend);
        HardMediumSoftLongScore tenDivisor = scoreDefinition.fromLevelNumbers(10, new Number[] { 10L, 10L, 10L });
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, tenDivisor))
                .isEqualTo(scoreDefinition.fromLevelNumbers(0, new Number[] { 0L, 0L, 1L }));
    }

}
