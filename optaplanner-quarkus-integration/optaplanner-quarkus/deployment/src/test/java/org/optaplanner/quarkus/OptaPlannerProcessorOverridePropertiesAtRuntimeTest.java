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

package org.optaplanner.quarkus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.quarkus.config.OptaPlannerRuntimeConfig;
import org.optaplanner.quarkus.config.SolverManagerRuntimeConfig;
import org.optaplanner.quarkus.config.SolverRuntimeConfig;
import org.optaplanner.quarkus.config.TerminationRuntimeConfig;
import org.optaplanner.quarkus.testdata.normal.constraints.TestdataQuarkusConstraintProvider;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusEntity;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusSolution;

import io.quarkus.test.QuarkusUnitTest;

public class OptaPlannerProcessorOverridePropertiesAtRuntimeTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            // We want to check if these are overridden by our runtime alternative
            .overrideConfigKey("quarkus.optaplanner.solver.termination.best-score-limit", "0")
            .overrideConfigKey("quarkus.optaplanner.solver.move-thread-count", "4")
            .overrideConfigKey("quarkus.optaplanner.solver-manager.parallel-solver-count", "1")
            // MyOptaPlannerRuntimeConfig is an alternative for OptaPlannerRuntimeConfig,
            // and simulates setting the values at runtime.
            .overrideConfigKey("quarkus.arc.selected-alternatives", "MyOptaPlannerRuntimeConfig")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class,
                            TestdataQuarkusSolution.class,
                            TestdataQuarkusConstraintProvider.class,
                            MyOptaPlannerRuntimeConfig.class));

    @Inject
    SolverConfig solverConfig;

    @Inject
    SolverManagerConfig solverManagerConfig;

    @Test
    public void solverConfigPropertiesShouldBeOverwritten() {
        assertNotNull(solverConfig);
        assertEquals("7", solverConfig.getTerminationConfig().getBestScoreLimit());
        assertEquals("3", solverConfig.getMoveThreadCount());
    }

    @Test
    public void solverManagerConfigPropertiesShouldBeOverwritten() {
        assertNotNull(solverManagerConfig);
        assertEquals("10", solverManagerConfig.getParallelSolverCount());
    }

    @Alternative
    @ApplicationScoped
    public static class MyOptaPlannerRuntimeConfig extends OptaPlannerRuntimeConfig {
        public MyOptaPlannerRuntimeConfig() {
            solver = new SolverRuntimeConfig();
            solver.termination = new TerminationRuntimeConfig();
            solver.termination.bestScoreLimit = Optional.of("7");
            solver.termination.spentLimit = Optional.empty();
            solver.termination.unimprovedSpentLimit = Optional.empty();
            solver.moveThreadCount = Optional.of("3");

            solverManager = new SolverManagerRuntimeConfig();
            solverManager.parallelSolverCount = Optional.of("10");
        }
    }

}
