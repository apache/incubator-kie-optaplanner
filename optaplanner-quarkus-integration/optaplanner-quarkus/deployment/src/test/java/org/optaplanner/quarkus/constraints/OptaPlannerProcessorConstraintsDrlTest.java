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

package org.optaplanner.quarkus.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.quarkus.deployment.config.OptaPlannerBuildTimeConfig;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusEntity;
import org.optaplanner.quarkus.testdata.normal.domain.TestdataQuarkusSolution;

import io.quarkus.test.QuarkusUnitTest;

@Disabled("PLANNER-2914")
class OptaPlannerProcessorConstraintsDrlTest {

    private static final String CONSTRAINTS_DRL = "customConstraints.drl";

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestdataQuarkusEntity.class, TestdataQuarkusSolution.class)
                    .addAsResource("org/optaplanner/quarkus/constraints/defaultConstraints.drl", CONSTRAINTS_DRL))
            .overrideConfigKey(OptaPlannerBuildTimeConfig.CONSTRAINTS_DRL_PROPERTY, CONSTRAINTS_DRL);

    @Inject
    SolverConfig solverConfig;

    @Inject
    SolverFactory<TestdataQuarkusSolution> solverFactory;

    @Test
    void constraintsDrl() {
        assertEquals(Collections.singletonList(CONSTRAINTS_DRL),
                solverConfig.getScoreDirectorFactoryConfig().getScoreDrlList());
        assertNotNull(solverFactory.buildSolver());
    }
}
