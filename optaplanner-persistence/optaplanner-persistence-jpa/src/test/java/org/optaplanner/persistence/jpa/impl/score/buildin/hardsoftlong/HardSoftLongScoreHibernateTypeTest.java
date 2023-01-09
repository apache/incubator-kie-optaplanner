package org.optaplanner.persistence.jpa.impl.score.buildin.hardsoftlong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HardSoftLongScoreHibernateTypeTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new HardSoftLongScoreHibernateTypeTestJpaEntity(HardSoftLongScore.ZERO),
                HardSoftLongScore.of(-10L, -2L),
                HardSoftLongScore.ofUninitialized(-7, -10L, -2L));
    }

    @Entity
    @TypeDef(defaultForType = HardSoftLongScore.class, typeClass = HardSoftLongScoreHibernateType.class)
    static class HardSoftLongScoreHibernateTypeTestJpaEntity extends AbstractTestJpaEntity<HardSoftLongScore> {

        @Columns(columns = { @Column(name = "initScore"), @Column(name = "hardScore"), @Column(name = "softScore") })
        protected HardSoftLongScore score;

        HardSoftLongScoreHibernateTypeTestJpaEntity() {
        }

        public HardSoftLongScoreHibernateTypeTestJpaEntity(HardSoftLongScore score) {
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
