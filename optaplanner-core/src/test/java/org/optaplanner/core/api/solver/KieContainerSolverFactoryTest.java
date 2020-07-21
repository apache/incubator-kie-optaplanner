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

package org.optaplanner.core.api.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Collections;

import org.drools.compiler.CommonTestMethodBase;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirectorFactory;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.util.KieContainerHelper;

public class KieContainerSolverFactoryTest extends CommonTestMethodBase {

    private final KieContainerHelper kieContainerHelper = new KieContainerHelper();

    @Test
    public void buildSolverWithReleaseId() throws IOException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithReleaseId",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionTestdataSolverConfig.xml");
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                releaseId, "testdata/kjar/solverConfig.solver");
        Solver<TestdataSolution> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildSolverWithKieContainer() throws IOException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithKieContainer",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionTestdataSolverConfig.xml");
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                kieContainer, "testdata/kjar/solverConfig.solver");
        Solver<TestdataSolution> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildScanAnnotatedClassesSolver() throws IOException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildScanAnnotatedClassesSolver",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/scanAnnotatedKieContainerTestdataSolverConfig.xml");
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                releaseId, "testdata/kjar/solverConfig.solver");
        Solver<TestdataSolution> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildScanAnnotatedClassesSolverWithFailure() throws IOException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildScanAnnotatedClassesSolver",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/scanAnnotatedKieContainerTestdataSolverConfigFailure.xml");
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                kieContainer, "testdata/kjar/solverConfig.solver");

        String error = "The scanAnnotatedClasses " +
                "(ScanAnnotatedClassesConfig([testdata.kjar], [testdata.kjar])) did not find any classes with a PlanningSolution annotation";
        assertThatThrownBy(solverFactory::buildSolver).hasMessageStartingWith(error);
    }

    @Test
    public void buildScanAnnotatedClassesSolverWithManySolutionsSuccess() throws IOException {
        Resource secondSolution = kieContainerHelper.buildResource(
                "org/optaplanner/core/impl/testdata/domain/classloaded/ClassloadedTestdataSecondSolution.java",
                "testdata/second/kjar/ClassloadedTestdataSecondSolution.java");
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildScanAnnotatedClassesSolver",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/scanAnnotatedMultiplePackagesKieContainerTestdataSolverConfig.xml",
                secondSolution);
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        SolverFactory<TestdataSolution> solverFactory =
                SolverFactory.createFromKieContainerXmlResource(kieContainer, "testdata/kjar/solverConfig.solver");
        Solver<TestdataSolution> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildScanAnnotatedClassesSolverWithManySolutionsFailure() throws IOException {
        Resource secondSolution = kieContainerHelper.buildResource(
                "org/optaplanner/core/impl/testdata/domain/classloaded/ClassloadedTestdataSecondSolution.java",
                "testdata/second/kjar/ClassloadedTestdataSecondSolution.java");
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildScanAnnotatedClassesSolver",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/scanAnnotatedMultiplePackagesKieContainerTestdataSolverConfigFailure.xml",
                secondSolution);
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                kieContainer, "testdata/kjar/solverConfig.solver");

        assertThatThrownBy(solverFactory::buildSolver)
                .hasMessageStartingWith(
                        "The scanAnnotatedClasses (ScanAnnotatedClassesConfig([testdata.kjar, testdata.second.kjar], )) found multiple classes")
                .hasMessageContaining("class testdata.kjar.ClassloadedTestdataSolution")
                .hasMessageContaining("class testdata.second.kjar.ClassloadedTestdataSecondSolution")
                .hasMessageEndingWith("with a PlanningSolution annotation.");
    }

    @Test
    public void buildSolverWithDefaultKsessionKmodule() throws IOException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithDefaultKsessionKmodule",
                "org/optaplanner/core/api/solver/kieContainerDefaultKsessionKmodule.xml",
                "org/optaplanner/core/api/solver/kieContainerDefaultKsessionTestdataSolverConfig.xml");
        SolverFactory<?> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                releaseId, "testdata/kjar/solverConfig.solver");
        Solver<?> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildSolverWithEmptyKmodule()
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithEmptyKmodule",
                "org/optaplanner/core/api/solver/kieContainerEmptyKmodule.xml",
                "org/optaplanner/core/api/solver/kieContainerDefaultKsessionTestdataSolverConfig.xml");
        SolverFactory<?> solverFactory = SolverFactory.createFromKieContainerXmlResource(
                releaseId, "testdata/kjar/solverConfig.solver");
        Solver<?> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildSolverWithKieContainerAndConfig() throws IOException, ReflectiveOperationException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithReleaseId",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml", null);
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setSolutionClass(
                kieContainer.getClassLoader().loadClass("testdata.kjar.ClassloadedTestdataSolution"));
        solverConfig.setEntityClassList(Collections.singletonList(
                kieContainer.getClassLoader().loadClass("testdata.kjar.ClassloadedTestdataEntity")));
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setKsessionName("testdataKsession");
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        SolverFactory<?> solverFactory = SolverFactory.createFromKieContainer(kieContainer, solverConfig);
        Solver<?> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    @Test
    public void buildSolverEmptyWithKieContainer() throws IOException, ReflectiveOperationException {
        ReleaseId releaseId = kieContainerHelper.deployTestdataSolverKjar(
                "buildSolverWithReleaseId",
                "org/optaplanner/core/api/solver/kieContainerNamedKsessionKmodule.xml", null);
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        SolverFactory<?> solverFactory = SolverFactory.createEmptyFromKieContainer(kieContainer);
        SolverConfig solverConfig = solverFactory.getSolverConfig();
        solverConfig.setSolutionClass(
                kieContainer.getClassLoader().loadClass("testdata.kjar.ClassloadedTestdataSolution"));
        solverConfig.setEntityClassList(Collections.singletonList(
                kieContainer.getClassLoader().loadClass("testdata.kjar.ClassloadedTestdataEntity")));
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setKsessionName("testdataKsession");
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);
        Solver<?> solver = solverFactory.buildSolver();
        assertThat(solver).isNotNull();
        assertNewKieSessionSucceeds(solver);
    }

    private void assertNewKieSessionSucceeds(Solver<?> solver) {
        DefaultSolver<?> defaultSolver = (DefaultSolver<?>) solver;
        DroolsScoreDirectorFactory scoreDirectorFactory = (DroolsScoreDirectorFactory<?>) defaultSolver
                .getScoreDirectorFactory();
        scoreDirectorFactory.newKieSession();
    }

}
