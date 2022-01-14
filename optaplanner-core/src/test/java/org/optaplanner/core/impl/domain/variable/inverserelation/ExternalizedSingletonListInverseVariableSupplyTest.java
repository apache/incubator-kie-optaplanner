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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class ExternalizedSingletonListInverseVariableSupplyTest {

    @Test
    void listVariable() {
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();
        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ExternalizedSingletonListInverseVariableSupply<TestdataListSolution> supply =
                new ExternalizedSingletonListInverseVariableSupply<>(variableDescriptor);

        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        TestdataListSolution solution = new TestdataListSolution();
        solution.setEntityList(new ArrayList<>(Arrays.asList(e1, e2)));
        solution.setValueList(Arrays.asList(v1, v2, v3));

        when(scoreDirector.getWorkingSolution()).thenReturn(solution);
        supply.resetWorkingSolution(scoreDirector);

        // Inverse variable is set immediately after the working solution is reset.
        assertThat(supply.getInverseSingleton(v1)).isSameAs(e1);
        assertThat(supply.getInverseSingleton(v2)).isSameAs(e1);
        assertThat(supply.getInverseSingleton(v3)).isSameAs(e2);

        // Move v1 from e1[0] to e2[1].
        supply.beforeVariableChanged(scoreDirector, e1);
        e1.getValueList().remove(v1);
        supply.afterVariableChanged(scoreDirector, e1);
        supply.beforeVariableChanged(scoreDirector, e2);
        e2.getValueList().add(v1);
        supply.afterVariableChanged(scoreDirector, e2);

        assertThat(supply.getInverseSingleton(v1)).isSameAs(e2);

        // Remove e2.
        supply.beforeEntityRemoved(scoreDirector, e2);
        solution.getEntityList().remove(e2);
        supply.afterEntityRemoved(scoreDirector, e2);

        assertThat(supply.getInverseSingleton(v1)).isNull();
        assertThat(supply.getInverseSingleton(v3)).isNull();

        // Unassign v2 from e1.
        supply.beforeVariableChanged(scoreDirector, e1);
        e1.getValueList().remove(v2);
        supply.afterVariableChanged(scoreDirector, e1);

        assertThat(supply.getInverseSingleton(v2)).isNull();

        supply.close();
    }
}
