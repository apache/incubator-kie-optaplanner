/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.domain.variable.inverserelation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class SingletonListInverseVariableListenerTest {

    @Test
    void inverseRelation() {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);

        SingletonListInverseVariableListener<TestdataListSolution> inverseVariableListener =
                new SingletonListInverseVariableListener<>(
                        TestdataListValue.buildVariableDescriptorForEntity(),
                        TestdataListEntity.buildVariableDescriptorForValueList());

        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("a", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("b", v3);

        assertThat(v1.getEntity()).isNull();
        assertThat(v2.getEntity()).isNull();
        assertThat(v3.getEntity()).isNull();

        inverseVariableListener.beforeEntityAdded(scoreDirector, e1);
        inverseVariableListener.afterEntityAdded(scoreDirector, e1);
        inverseVariableListener.beforeEntityAdded(scoreDirector, e2);
        inverseVariableListener.afterEntityAdded(scoreDirector, e2);

        assertThat(v1.getEntity()).isEqualTo(e1);
        assertThat(v2.getEntity()).isEqualTo(e1);
        assertThat(v3.getEntity()).isEqualTo(e2);
        assertThat(inverseVariableListener.getInverseSingleton(v1)).isEqualTo(e1);
        assertThat(inverseVariableListener.getInverseSingleton(v2)).isEqualTo(e1);
        assertThat(inverseVariableListener.getInverseSingleton(v3)).isEqualTo(e2);

        inverseVariableListener.beforeVariableChanged(scoreDirector, e1);
        e1.getValueList().remove(v1);
        inverseVariableListener.afterVariableChanged(scoreDirector, e1);
        inverseVariableListener.beforeVariableChanged(scoreDirector, e2);
        e2.getValueList().add(v1);
        inverseVariableListener.afterVariableChanged(scoreDirector, e2);

        assertThat(v1.getEntity()).isEqualTo(e2);
        assertThat(inverseVariableListener.getInverseSingleton(v1)).isEqualTo(e2);
    }

    @Test
    void removeEntity() {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);

        SingletonListInverseVariableListener<TestdataListSolution> inverseVariableListener =
                new SingletonListInverseVariableListener<>(
                        TestdataListValue.buildVariableDescriptorForEntity(),
                        TestdataListEntity.buildVariableDescriptorForValueList());

        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = TestdataListEntity.createWithValues("a", v1, v2);
        TestdataListEntity e2 = TestdataListEntity.createWithValues("b", v3);

        assertThat(v1.getEntity()).isEqualTo(e1);
        assertThat(v2.getEntity()).isEqualTo(e1);
        assertThat(v3.getEntity()).isEqualTo(e2);

        inverseVariableListener.beforeEntityRemoved(scoreDirector, e1);
        inverseVariableListener.afterEntityRemoved(scoreDirector, e1);

        assertThat(v1.getEntity()).isNull();
        assertThat(v2.getEntity()).isNull();
        assertThat(v3.getEntity()).isEqualTo(e2);
        assertThat(inverseVariableListener.getInverseSingleton(v1)).isNull();
        assertThat(inverseVariableListener.getInverseSingleton(v2)).isNull();
        assertThat(inverseVariableListener.getInverseSingleton(v3)).isEqualTo(e2);
    }
}
