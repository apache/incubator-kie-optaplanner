/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.domain.variable.index;

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

class ExternalizedIndexVariableSupplyTest {

    @Test
    void listVariable() {
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();
        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ExternalizedIndexVariableSupply<TestdataListSolution> supply =
                new ExternalizedIndexVariableSupply<>(variableDescriptor);

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

        // Indexes are set immediately after the working solution is reset.
        assertThat(supply.getIndex(v1)).isEqualTo(0);
        assertThat(supply.getIndex(v2)).isEqualTo(1);
        assertThat(supply.getIndex(v3)).isEqualTo(0);

        // Move v3 from e2[0] to e1[2].
        supply.beforeListVariableChanged(scoreDirector, e2, 0, 1);
        e2.getValueList().remove(v3);
        supply.afterListVariableChanged(scoreDirector, e2, 0, 0);
        supply.beforeListVariableChanged(scoreDirector, e1, 2, 2);
        e1.getValueList().add(v3);
        supply.afterListVariableChanged(scoreDirector, e1, 2, 3);

        assertThat(supply.getIndex(v3)).isEqualTo(2);

        // Unassign v1 from e1.
        supply.beforeListVariableChanged(scoreDirector, e1, 0, 1);
        e1.getValueList().remove(v1);
        supply.afterListVariableElementUnassigned(scoreDirector, v1);
        supply.afterListVariableChanged(scoreDirector, e1, 0, 0);

        assertThat(supply.getIndex(v1)).isNull();
        assertThat(supply.getIndex(v2)).isEqualTo(0);
        assertThat(supply.getIndex(v3)).isEqualTo(1);

        // Remove e1.
        supply.beforeEntityRemoved(scoreDirector, e1);
        solution.getEntityList().remove(e1);
        supply.afterEntityRemoved(scoreDirector, e1);

        assertThat(supply.getIndex(v2)).isNull();
        assertThat(supply.getIndex(v3)).isNull();

        // Assign v1 to e2.
        supply.beforeListVariableChanged(scoreDirector, e2, 0, 0);
        e2.getValueList().add(0, v1);
        supply.afterListVariableChanged(scoreDirector, e2, 0, 1);

        assertThat(supply.getIndex(v1)).isEqualTo(0);

        // Return e1.
        supply.beforeEntityAdded(scoreDirector, e1);
        solution.getEntityList().add(e1);
        supply.afterEntityAdded(scoreDirector, e1);

        assertThat(supply.getIndex(v2)).isEqualTo(0);
        assertThat(supply.getIndex(v3)).isEqualTo(1);

        // Move subList e1[0..2] to e2[1].
        supply.beforeListVariableChanged(scoreDirector, e1, 0, 0);
        supply.beforeListVariableChanged(scoreDirector, e2, 1, 3);
        e2.getValueList().addAll(e1.getValueList());
        e1.getValueList().clear();
        supply.afterListVariableChanged(scoreDirector, e1, 0, 0);
        supply.afterListVariableChanged(scoreDirector, e2, 1, 3);

        assertThat(supply.getIndex(v1)).isEqualTo(0);
        assertThat(supply.getIndex(v2)).isEqualTo(1);
        assertThat(supply.getIndex(v3)).isEqualTo(2);

        supply.close();
    }
}
