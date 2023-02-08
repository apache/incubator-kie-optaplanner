package org.optaplanner.persistence.jpa.impl.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HardSoftBigDecimalScoreHibernateTypeTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new HardSoftBigDecimalScoreHibernateTypeTestJpaEntity(HardSoftBigDecimalScore.ZERO),
                HardSoftBigDecimalScore.of(new BigDecimal("-10.01000"), new BigDecimal("-2.20000")),
                HardSoftBigDecimalScore.ofUninitialized(-7, new BigDecimal("-10.01000"), new BigDecimal("-2.20000")));
    }

    @Entity
    @TypeDef(defaultForType = HardSoftBigDecimalScore.class, typeClass = HardSoftBigDecimalScoreHibernateType.class)
    static class HardSoftBigDecimalScoreHibernateTypeTestJpaEntity
            extends AbstractTestJpaEntity<HardSoftBigDecimalScore> {

        @Columns(columns = {
                @Column(name = "initScore"),
                @Column(name = "hardScore", precision = 10, scale = 5),
                @Column(name = "softScore", precision = 10, scale = 5) })
        protected HardSoftBigDecimalScore score;

        HardSoftBigDecimalScoreHibernateTypeTestJpaEntity() {
        }

        public HardSoftBigDecimalScoreHibernateTypeTestJpaEntity(HardSoftBigDecimalScore score) {
            this.score = score;
        }

        @Override
        public HardSoftBigDecimalScore getScore() {
            return score;
        }

        @Override
        public void setScore(HardSoftBigDecimalScore score) {
            this.score = score;
        }

    }

}
