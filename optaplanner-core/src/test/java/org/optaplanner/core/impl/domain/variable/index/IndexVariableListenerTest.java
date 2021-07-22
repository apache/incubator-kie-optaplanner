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

package org.optaplanner.core.impl.domain.variable.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class IndexVariableListenerTest {

    @Test
    void index() {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        SolutionDescriptor<TestdataListSolution> solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
        EntityDescriptor<TestdataListSolution> sourceEntityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataListEntity.class);
        EntityDescriptor<TestdataListSolution> shadowEntityDescriptor =
                solutionDescriptor.findEntityDescriptorOrFail(TestdataListValue.class);
        IndexShadowVariableDescriptor<TestdataListSolution> indexShadowVariableDescriptor =
                (IndexShadowVariableDescriptor<TestdataListSolution>) shadowEntityDescriptor
                        .getShadowVariableDescriptor("index");

        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor =
                (ListVariableDescriptor<TestdataListSolution>) sourceEntityDescriptor.getGenuineVariableDescriptor("valueList");
        IndexVariableListener<TestdataListSolution> indexVariableListener =
                new IndexVariableListener<>(indexShadowVariableDescriptor, listVariableDescriptor);

        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListEntity entity = new TestdataListEntity("a", v1, v2, v3);

        assertThat(v1.getIndex()).isNull();
        assertThat(v2.getIndex()).isNull();
        assertThat(v3.getIndex()).isNull();

        indexVariableListener.beforeEntityAdded(scoreDirector, entity);
        indexVariableListener.afterEntityAdded(scoreDirector, entity);

        assertThat(v1.getIndex()).isEqualTo(0);
        assertThat(v2.getIndex()).isEqualTo(1);
        assertThat(v3.getIndex()).isEqualTo(2);

        indexVariableListener.beforeVariableChanged(scoreDirector, entity);
        entity.getValueList().add(2, v4);
        indexVariableListener.afterVariableChanged(scoreDirector, entity);

        assertThat(v1.getIndex()).isEqualTo(0);
        assertThat(v2.getIndex()).isEqualTo(1);
        assertThat(v4.getIndex()).isEqualTo(2);
        assertThat(v3.getIndex()).isEqualTo(3);

        indexVariableListener.beforeVariableChanged(scoreDirector, entity);
        entity.getValueList().remove(v1);
        indexVariableListener.afterVariableChanged(scoreDirector, entity);

        assertThat(v2.getIndex()).isEqualTo(0);
        assertThat(v4.getIndex()).isEqualTo(1);
        assertThat(v3.getIndex()).isEqualTo(2);
    }
}
