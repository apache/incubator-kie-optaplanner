package org.optaplanner.persistence.jpa.impl.score.buildin.simple;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.persistence.jpa.impl.AbstractScoreJpaTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SimpleScoreHibernateTypeTest extends AbstractScoreJpaTest {

    @Test
    void persistAndMerge() {
        persistAndMerge(new SimpleScoreHibernateTypeTestJpaEntity(SimpleScore.ZERO),
                SimpleScore.of(-10),
                SimpleScore.ofUninitialized(-7, -10));
    }

    @Entity
    @TypeDef(defaultForType = SimpleScore.class, typeClass = SimpleScoreHibernateType.class)
    static class SimpleScoreHibernateTypeTestJpaEntity extends AbstractTestJpaEntity<SimpleScore> {

        @Columns(columns = { @Column(name = "initScore"), @Column(name = "score") })
        protected SimpleScore score;

        SimpleScoreHibernateTypeTestJpaEntity() {
        }

        public SimpleScoreHibernateTypeTestJpaEntity(SimpleScore score) {
            this.score = score;
        }

        @Override
        public SimpleScore getScore() {
            return score;
        }

        @Override
        public void setScore(SimpleScore score) {
            this.score = score;
        }

    }

}
