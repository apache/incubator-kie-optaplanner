/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.api;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.optaplanner.benchmark.config.SolverBenchmarkConfig;
import org.optaplanner.core.api.solver.DivertingClassLoader;
import org.optaplanner.core.config.phase.custom.CustomPhaseConfig;
import org.optaplanner.core.impl.phase.custom.DummyCustomPhaseCommand;

import static org.assertj.core.api.Assertions.assertThat;

public class PlannerBenchmarkFactoryTest {

    @BeforeClass
    public static void setup() throws IOException {
        File benchmarkTestDir = new File("target/benchmarkTest/");
        benchmarkTestDir.mkdirs();
        new File(benchmarkTestDir, "input.xml").createNewFile();
        new File(benchmarkTestDir, "output/").mkdir();
    }

    @Test
    public void benchmarkConfig() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/optaplanner/benchmark/api/testdataPlannerBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingBenchmarkConfig() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/optaplanner/benchmark/api/nonExistingPlannerBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test
    public void uninitializedBenchmarkResult() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/optaplanner/benchmark/api/testdataPlannerBenchmarkConfig.xml");
        SolverBenchmarkConfig solverBenchmarkConfig = plannerBenchmarkFactory.getPlannerBenchmarkConfig().getSolverBenchmarkConfigList().get(0);
        CustomPhaseConfig phaseConfig = new CustomPhaseConfig();
        phaseConfig.setCustomPhaseCommandClassList(Collections.singletonList(DummyCustomPhaseCommand.class));
        solverBenchmarkConfig.getSolverConfig() .setPhaseConfigList(Collections.singletonList(phaseConfig));
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test
    public void subsingleBenchmarkConfig() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/optaplanner/benchmark/api/testdataPlannerBenchmarkConfig.xml");
        SolverBenchmarkConfig solverBenchmarkConfig = plannerBenchmarkFactory.getPlannerBenchmarkConfig().getSolverBenchmarkConfigList().get(0);
        solverBenchmarkConfig.setSubSingleCount(3);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test
    public void benchmarkConfigWithClassLoader() {
        // Mocking loadClass doesn't work well enough, because the className still differs from class.getName()
        ClassLoader classLoader = new DivertingClassLoader(getClass().getClassLoader());
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "divertThroughClassLoader/org/optaplanner/benchmark/api/classloaderTestdataPlannerBenchmarkConfig.xml", classLoader);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test
    public void template() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "org/optaplanner/benchmark/api/testdataPlannerBenchmarkConfigTemplate.xml.ftl");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingTemplate() {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "org/optaplanner/benchmark/api/nonExistingPlannerBenchmarkConfigTemplate.xml.ftl");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

    @Test
    public void templateWithClassLoader() {
        // Mocking loadClass doesn't work well enough, because the className still differs from class.getName()
        ClassLoader classLoader = new DivertingClassLoader(getClass().getClassLoader());
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "divertThroughClassLoader/org/optaplanner/benchmark/api/classloaderTestdataPlannerBenchmarkConfigTemplate.xml.ftl", classLoader);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        assertThat(plannerBenchmark).isNotNull();
        plannerBenchmark.benchmark();
    }

}
