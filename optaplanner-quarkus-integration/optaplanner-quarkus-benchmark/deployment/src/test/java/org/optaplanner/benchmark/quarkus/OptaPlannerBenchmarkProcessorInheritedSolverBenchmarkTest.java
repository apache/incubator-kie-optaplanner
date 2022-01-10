/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.quarkus;

import java.util.List;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;
import org.optaplanner.benchmark.quarkus.testdata.normal.constraints.TestdataQuarkusConstraintProvider;
import org.optaplanner.benchmark.quarkus.testdata.normal.domain.TestdataQuarkusEntity;
import org.optaplanner.benchmark.quarkus.testdata.normal.domain.TestdataQuarkusSolution;

import io.quarkus.test.QuarkusUnitTest;

public class OptaPlannerBenchmarkProcessorInheritedSolverBenchmarkTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.optaplanner.benchmark.solver.termination.best-score-limit", "0")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class,
                            TestdataQuarkusSolution.class, TestdataQuarkusConstraintProvider.class)
                    .addAsResource("solverConfig.xml")
                    .addAsResource("solverBenchmarkConfigWithInheritedSolverBenchmark.xml", "solverBenchmarkConfig.xml"));

    @Inject
    PlannerBenchmarkConfig plannerBenchmarkConfig;

    @Test
    public void inheritClassesFromSolverConfig() {
        Assertions.assertEquals(TestdataQuarkusSolution.class,
                plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig().getSolverConfig().getSolutionClass());
        Assertions.assertEquals(List.of(TestdataQuarkusEntity.class),
                plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig().getSolverConfig().getEntityClassList());
        Assertions.assertEquals(5, plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig().getSolverConfig()
                .getTerminationConfig().getMillisecondsSpentLimit());
        Assertions.assertNotNull(plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig().getProblemBenchmarksConfig());
    }

}
