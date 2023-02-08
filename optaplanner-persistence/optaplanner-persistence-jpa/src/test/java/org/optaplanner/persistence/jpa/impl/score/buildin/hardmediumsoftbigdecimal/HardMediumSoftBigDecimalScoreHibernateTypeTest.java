package org.optaplanner.persistence.jpa.impl.score.buildin.hardmediumsoftbigdecimal;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HardMediumSoftBigDecimalScoreHibernateTypeTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new HardMediumSoftBigDecimalScoreHibernateTypeTestJpaEntity(HardMediumSoftBigDecimalScore.ZERO),
                HardMediumSoftBigDecimalScore.of(new BigDecimal("-10.01000"), new BigDecimal("-4.32100"),
                        new BigDecimal("-2.20000")),
                HardMediumSoftBigDecimalScore.ofUninitialized(-7, new BigDecimal("-10.01000"), new BigDecimal("-4.32100"),
                        new BigDecimal("-2.20000")));
    }

    @Entity
    @TypeDef(defaultForType = HardMediumSoftBigDecimalScore.class, typeClass = HardMediumSoftBigDecimalScoreHibernateType.class)
    static class HardMediumSoftBigDecimalScoreHibernateTypeTestJpaEntity
            extends AbstractTestJpaEntity<HardMediumSoftBigDecimalScore> {

        @Columns(columns = {
                @Column(name = "initScore"),
                @Column(name = "hardScore", precision = 10, scale = 5),
                @Column(name = "mediumScore", precision = 10, scale = 5),
                @Column(name = "softScore", precision = 10, scale = 5) })
        protected HardMediumSoftBigDecimalScore score;

        HardMediumSoftBigDecimalScoreHibernateTypeTestJpaEntity() {
        }

        public HardMediumSoftBigDecimalScoreHibernateTypeTestJpaEntity(HardMediumSoftBigDecimalScore score) {
            this.score = score;
        }

        @Override
        public HardMediumSoftBigDecimalScore getScore() {
            return score;
        }

        @Override
        public void setScore(HardMediumSoftBigDecimalScore score) {
            this.score = score;
        }

    }

}
