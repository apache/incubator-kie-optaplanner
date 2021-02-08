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
package org.optaplanner.core.impl.domain.solution.cloner;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.solution.cloner.SolutionCloner;
import org.optaplanner.core.impl.domain.solution.cloner.gizmo.GizmoSolutionClonerFactory;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.extended.TestdataUnannotatedExtendedEntity;
import org.optaplanner.core.impl.testdata.domain.extended.TestdataUnannotatedExtendedSolution;

public class GizmoSolutionClonerTest extends AbstractSolutionClonerTest {

    @Override
    protected <Solution_> SolutionCloner<Solution_> createSolutionCloner(SolutionDescriptor<Solution_> solutionDescriptor) {
        return GizmoSolutionClonerFactory.build(solutionDescriptor);
    }

    // This test verify a proper error message is thrown if an extended solution is passed.
    @Override
    @Test
    public void cloneExtendedSolution() {
        SolutionDescriptor solutionDescriptor = TestdataUnannotatedExtendedSolution.buildSolutionDescriptor();
        SolutionCloner<TestdataUnannotatedExtendedSolution> cloner = createSolutionCloner(solutionDescriptor);

        TestdataValue val1 = new TestdataValue("1");
        TestdataValue val2 = new TestdataValue("2");
        TestdataValue val3 = new TestdataValue("3");
        TestdataUnannotatedExtendedEntity a = new TestdataUnannotatedExtendedEntity("a", val1, null);
        TestdataUnannotatedExtendedEntity b = new TestdataUnannotatedExtendedEntity("b", val1, "extraObjectOnEntity");
        TestdataUnannotatedExtendedEntity c = new TestdataUnannotatedExtendedEntity("c", val3);
        TestdataUnannotatedExtendedEntity d = new TestdataUnannotatedExtendedEntity("d", val3, c);
        c.setExtraObject(d);

        TestdataUnannotatedExtendedSolution original = new TestdataUnannotatedExtendedSolution("solution",
                "extraObjectOnSolution");
        List<TestdataValue> valueList = Arrays.asList(val1, val2, val3);
        original.setValueList(valueList);
        List<TestdataEntity> originalEntityList = Arrays.asList(a, b, c, d);
        original.setEntityList(originalEntityList);

        assertThatCode(() -> cloner.cloneSolution(original))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Failed to create clone: encountered (" + original.getClass() + ") which is not a known subclass of " +
                                "the solution class (" + TestdataSolution.class + "). The known subclasses are " +
                                "[" + TestdataSolution.class.getName() + "].\nMaybe use DomainAccessType.REFLECTION?");
    }
}
