package org.optaplanner.persistence.jpa.impl.score.buildin.bendablelong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BendableLongScoreHibernateTypeTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new BendableLongScoreHibernateTypeTestJpaEntity(BendableLongScore.zero(3, 2)),
                BendableLongScore.of(new long[] { 10000L, 2000L, 300L }, new long[] { 40L, 5L }),
                BendableLongScore.ofUninitialized(-7, new long[] { 10000L, 2000L, 300L }, new long[] { 40L, 5L }));
    }

    @Entity
    @TypeDef(defaultForType = BendableLongScore.class, typeClass = BendableLongScoreHibernateType.class, parameters = {
            @Parameter(name = "hardLevelsSize", value = "3"), @Parameter(name = "softLevelsSize", value = "2") })
    static class BendableLongScoreHibernateTypeTestJpaEntity extends AbstractTestJpaEntity<BendableLongScore> {

        @Columns(columns = { @Column(name = "initScore"),
                @Column(name = "hard0Score"), @Column(name = "hard1Score"), @Column(name = "hard2Score"),
                @Column(name = "soft0Score"), @Column(name = "soft1Score") })
        protected BendableLongScore score;

        BendableLongScoreHibernateTypeTestJpaEntity() {
        }

        public BendableLongScoreHibernateTypeTestJpaEntity(BendableLongScore score) {
            this.score = score;
        }

        @Override
        public BendableLongScore getScore() {
            return score;
        }

        @Override
        public void setScore(BendableLongScore score) {
            this.score = score;
        }

    }

}
