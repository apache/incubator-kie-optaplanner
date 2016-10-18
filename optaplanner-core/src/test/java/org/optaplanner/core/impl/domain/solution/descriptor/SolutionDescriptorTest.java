/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.domain.solution.descriptor;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.collection.TestdataArrayBasedSolution;
import org.optaplanner.core.impl.testdata.domain.collection.TestdataSetBasedSolution;
import org.optaplanner.core.impl.testdata.domain.extended.TestdataAnnotatedExtendedSolution;
import org.optaplanner.core.impl.testdata.domain.extended.abstractsolution.TestdataExtendedAbstractSolution;
import org.optaplanner.core.impl.testdata.domain.extended.abstractsolution.TestdataExtendedAbstractSolutionOverridesGetScore;
import org.optaplanner.core.impl.testdata.domain.extended.legacysolution.TestdataLegacySolution;
import org.optaplanner.core.impl.testdata.domain.reflect.generic.TestdataGenericSolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.TestdataNoProblemFactPropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.TestdataProblemFactPropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.TestdataReadMethodProblemFactCollectionPropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.invalid.TestdataDuplicatePlanningEntityCollectionPropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.invalid.TestdataDuplicatePlanningScorePropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.invalid.TestdataDuplicateProblemFactCollectionPropertySolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.invalid.TestdataProblemFactCollectionPropertyWithArgumentSolution;
import org.optaplanner.core.impl.testdata.domain.solutionproperties.invalid.TestdataProblemFactIsPlanningEntityCollectionPropertySolution;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.assertAllCodesOfCollection;

public class SolutionDescriptorTest {

    // ************************************************************************
    // Problem fact and planning entity properties
    // ************************************************************************

    @Test
    public void problemFactProperty() {
        SolutionDescriptor<TestdataProblemFactPropertySolution> solutionDescriptor
                = TestdataProblemFactPropertySolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).containsOnlyKeys("extraObject");
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("valueList", "otherProblemFactList");
    }

    @Test
    public void readMethodProblemFactCollectionProperty() {
        SolutionDescriptor<TestdataReadMethodProblemFactCollectionPropertySolution> solutionDescriptor
                = TestdataReadMethodProblemFactCollectionPropertySolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("valueList", "createProblemFacts");
    }

    @Test(expected = IllegalStateException.class)
    public void problemFactCollectionPropertyWithArgument() {
        TestdataProblemFactCollectionPropertyWithArgumentSolution.buildSolutionDescriptor();
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateProblemFactCollectionProperty() {
        TestdataDuplicateProblemFactCollectionPropertySolution.buildSolutionDescriptor();
    }

    @Test(expected = IllegalStateException.class)
    public void duplicatePlanningEntityCollectionProperty() {
        TestdataDuplicatePlanningEntityCollectionPropertySolution.buildSolutionDescriptor();
    }

    @Test(expected = IllegalStateException.class)
    public void duplicatePlanningScorePropertyProperty() {
        TestdataDuplicatePlanningScorePropertySolution.buildSolutionDescriptor();
    }

    @Test(expected = IllegalStateException.class)
    public void problemFactIsPlanningEntityCollectionProperty() {
        TestdataProblemFactIsPlanningEntityCollectionPropertySolution.buildSolutionDescriptor();
    }

    @Test
    public void noProblemFactPropertyWithEasyScoreCalculation() {
        SolverFactory<TestdataNoProblemFactPropertySolution> solverFactory
                = PlannerTestUtils.buildSolverFactory(
                        TestdataNoProblemFactPropertySolution.class, TestdataEntity.class);
        solverFactory.buildSolver();
    }

    @Test(expected = IllegalStateException.class)
    public void noProblemFactPropertyWithDroolsScoreCalculation() {
        SolverFactory<TestdataNoProblemFactPropertySolution> solverFactory
                = PlannerTestUtils.buildSolverFactoryWithDroolsScoreDirector(
                        TestdataNoProblemFactPropertySolution.class, TestdataEntity.class);
        solverFactory.buildSolver();
    }

    @Test
    public void extended() {
        SolutionDescriptor<TestdataAnnotatedExtendedSolution> solutionDescriptor
                = TestdataAnnotatedExtendedSolution.buildExtendedSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("valueList", "subValueList");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entityList", "subEntityList");
    }

    @Test
    public void setProperties() {
        SolutionDescriptor<TestdataSetBasedSolution> solutionDescriptor
                = TestdataSetBasedSolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("valueSet");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entitySet");
    }

    @Test @Ignore("Resolve PLANNER-573 to fix this")
    public void arrayProperties() {
        SolutionDescriptor<TestdataArrayBasedSolution> solutionDescriptor
                = TestdataArrayBasedSolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("values");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entities");
    }

    @Test
    public void generic() {
        SolutionDescriptor<TestdataGenericSolution> solutionDescriptor
                = TestdataGenericSolution.buildSolutionDescriptor();
    }

    // ************************************************************************
    // Others
    // ************************************************************************

    @Test
    public void extendedAbstractSolution() {
        SolutionDescriptor<TestdataExtendedAbstractSolution> solutionDescriptor
                = TestdataExtendedAbstractSolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("problemFactList");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entityList");

        TestdataExtendedAbstractSolution solution = new TestdataExtendedAbstractSolution();
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        solution.setExtraObject(new TestdataValue("extra"));
        solution.setEntityList(Arrays.asList(new TestdataEntity("e1"), new TestdataEntity("e2")));

        assertAllCodesOfCollection(solutionDescriptor.getAllFacts(solution), "e1", "e2", "v1", "v2", "extra");
    }

    @Test
    public void extendedAbstractSolutionOverridesGetScore() {
        SolutionDescriptor<TestdataExtendedAbstractSolutionOverridesGetScore> solutionDescriptor
                = TestdataExtendedAbstractSolutionOverridesGetScore.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("problemFactList");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entityList");

        TestdataExtendedAbstractSolutionOverridesGetScore solution = new TestdataExtendedAbstractSolutionOverridesGetScore();
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        solution.setExtraObject(new TestdataValue("extra"));
        solution.setEntityList(Arrays.asList(new TestdataEntity("e1"), new TestdataEntity("e2")));

        assertAllCodesOfCollection(solutionDescriptor.getAllFacts(solution), "e1", "e2", "v1", "v2", "extra");
    }

    @Test @Deprecated
    public void legacySolution() {
        SolutionDescriptor<TestdataLegacySolution> solutionDescriptor
                = TestdataLegacySolution.buildSolutionDescriptor();
        assertThat(solutionDescriptor.getProblemFactMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getProblemFactCollectionMemberAccessorMap()).containsOnlyKeys("problemFacts");
        assertThat(solutionDescriptor.getEntityMemberAccessorMap()).isEmpty();
        assertThat(solutionDescriptor.getEntityCollectionMemberAccessorMap()).containsOnlyKeys("entityList");
    }

}
