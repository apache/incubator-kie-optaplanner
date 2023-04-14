package org.optaplanner.persistence.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.optaplanner.core.api.score.Score;

public abstract class AbstractScoreJpaTest {

    @Inject
    EntityManagerFactory entityManagerFactory;

    protected <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> Long persistAndAssert(E jpaEntity) {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            // To avoid importing javax.transaction.*, which OpenRewrite fails to migrate.
            em.getTransaction().begin();
            em.persist(jpaEntity);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed.", e);
        }
        Long id = jpaEntity.getId();
        assertThat(id).isNotNull();
        return id;
    }

    @SafeVarargs
    protected final <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> void persistAndMerge(
            E jpaEntity, Score_... newScores) {
        Long id = persistAndAssert(jpaEntity);
        Class<? extends AbstractTestJpaEntity> jpaEntityClass = jpaEntity.getClass();
        Score_ oldScore = jpaEntity.getScore();
        for (Score_ newScore : newScores) {
            findAssertAndChangeScore(jpaEntityClass, id, oldScore, newScore);
            findAndAssert(jpaEntityClass, id, newScore);
            oldScore = newScore;
        }
    }

    protected <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> void findAssertAndChangeScore(
            Class<E> jpaEntityClass, Long id, Score_ oldScore, Score_ newScore) {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            // To avoid importing javax.transaction.*, which OpenRewrite fails to migrate.
            em.getTransaction().begin();
            E jpaEntity = em.find(jpaEntityClass, id);
            em.persist(jpaEntity);
            assertThat(jpaEntity.getScore()).isEqualTo(oldScore);
            jpaEntity.setScore(newScore);
            jpaEntity = em.merge(jpaEntity);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed.", e);
        }
    }

    protected <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> void findAndAssert(
            Class<E> jpaEntityClass, Long id, Score_ score) {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            // To avoid importing javax.transaction.*, which OpenRewrite fails to migrate.
            em.getTransaction().begin();
            E jpaEntity = em.find(jpaEntityClass, id);
            assertThat(jpaEntity.getScore()).isEqualTo(score);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed.", e);
        }
    }

    @MappedSuperclass
    protected static abstract class AbstractTestJpaEntity<Score_ extends Score<Score_>> {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        protected Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public abstract Score_ getScore();

        public abstract void setScore(Score_ score);

    }
}
