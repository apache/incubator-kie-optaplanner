package org.optaplanner.persistence.jpa.api.score.buildin.hardsoftlong;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HardSoftLongScoreConverterTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new HardSoftLongScoreConverterTestJpaEntity(HardSoftLongScore.ZERO), null,
                HardSoftLongScore.of(-10L, -2L),
                HardSoftLongScore.ofUninitialized(-7, -10L, -2L));
    }

    @Entity
    static class HardSoftLongScoreConverterTestJpaEntity extends AbstractTestJpaEntity<HardSoftLongScore> {

        @Convert(converter = HardSoftLongScoreConverter.class)
        protected HardSoftLongScore score;

        HardSoftLongScoreConverterTestJpaEntity() {
        }

        public HardSoftLongScoreConverterTestJpaEntity(HardSoftLongScore score) {
            this.score = score;
        }

        @Override
        public HardSoftLongScore getScore() {
            return score;
        }

        @Override
        public void setScore(HardSoftLongScore score) {
            this.score = score;
        }
    }
}
