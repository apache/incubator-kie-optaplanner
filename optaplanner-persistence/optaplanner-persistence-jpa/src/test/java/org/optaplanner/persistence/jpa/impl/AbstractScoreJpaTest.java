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

package org.optaplanner.persistence.jpa.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.kie.test.util.db.PersistenceUtil;
import org.optaplanner.core.api.score.Score;

public abstract class AbstractScoreJpaTest {

    protected Map<String, Object> context;
    protected EntityManagerFactory entityManagerFactory;
    protected TransactionManager transactionManager;

    @BeforeEach
    public void setUp() throws Exception {
        context = PersistenceUtil.setupWithPoolingDataSource("org.optaplanner.persistence.jpa.test");
        entityManagerFactory = (EntityManagerFactory) context.get(PersistenceUtil.ENTITY_MANAGER_FACTORY);
        transactionManager = InitialContext.doLookup("java:comp/TransactionManager");
    }

    @AfterEach
    public void tearDown() {
        PersistenceUtil.cleanUp(context);
    }

    protected <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> Long persistAndAssert(E jpaEntity) {
        try {
            transactionManager.begin();
            EntityManager em = entityManagerFactory.createEntityManager();
            em.persist(jpaEntity);
            transactionManager.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicRollbackException
                | HeuristicMixedException e) {
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
            transactionManager.begin();
            EntityManager em = entityManagerFactory.createEntityManager();
            E jpaEntity = em.find(jpaEntityClass, id);
            em.persist(jpaEntity);
            Assertions.assertThat(jpaEntity.getScore()).isEqualTo(oldScore);
            jpaEntity.setScore(newScore);
            jpaEntity = em.merge(jpaEntity);
            transactionManager.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicRollbackException
                | HeuristicMixedException e) {
            throw new RuntimeException("Transaction failed.", e);
        }
    }

    protected <Score_ extends Score<Score_>, E extends AbstractTestJpaEntity<Score_>> void findAndAssert(
            Class<E> jpaEntityClass, Long id, Score_ score) {
        try {
            transactionManager.begin();
            EntityManager em = entityManagerFactory.createEntityManager();
            E jpaEntity = em.find(jpaEntityClass, id);
            Assertions.assertThat(jpaEntity.getScore()).isEqualTo(score);
            transactionManager.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException e) {
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
