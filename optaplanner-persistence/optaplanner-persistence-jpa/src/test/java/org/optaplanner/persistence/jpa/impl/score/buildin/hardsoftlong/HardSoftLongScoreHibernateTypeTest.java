/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.jpa.impl.score.buildin.hardsoftlong;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.persistence.jpa.impl.score.AbstractScoreHibernateTypeTest;

public class HardSoftLongScoreHibernateTypeTest extends AbstractScoreHibernateTypeTest {

    @Test
    public void persistAndMerge() {
        persistAndMerge(new TestJpaEntity(HardSoftLongScore.ZERO),
                HardSoftLongScore.of(-10L, -2L),
                HardSoftLongScore.ofUninitialized(-7, -10L, -2L));
    }

    @Entity
    @TypeDef(defaultForType = HardSoftLongScore.class, typeClass = HardSoftLongScoreHibernateType.class)
    public static class TestJpaEntity extends AbstractTestJpaEntity<HardSoftLongScore> {

        protected HardSoftLongScore score;

        private TestJpaEntity() {
        }

        public TestJpaEntity(HardSoftLongScore score) {
            this.score = score;
        }

        @Override
        @Columns(columns = {@Column(name = "initScore"), @Column(name = "hardScore"), @Column(name = "softScore")})
        public HardSoftLongScore getScore() {
            return score;
        }

        @Override
        public void setScore(HardSoftLongScore score) {
            this.score = score;
        }

    }

}
