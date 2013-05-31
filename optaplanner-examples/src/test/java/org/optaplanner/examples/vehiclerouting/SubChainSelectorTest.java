/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.optaplanner.examples.vehiclerouting;

import java.io.File;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.examples.common.app.LoggingTest;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.vehiclerouting.domain.VrpSchedule;
import org.optaplanner.examples.vehiclerouting.persistence.VehicleRoutingDao;

/**
 * BZ968327 reproducer.
 * 
 * Problem is probably with solution initialized to single one chain of entities (with planning variable as the anchor).
 * @author rsynek
 */
public class SubChainSelectorTest extends LoggingTest {

    private static final String SOLVER_CONFIG = "/org/optaplanner/examples/vehiclerouting/solver-config.xml";
    private static final String DATASET = "unsolved/A-n32-k5.xml";

    @Test
    public void testSubchainEqualMinMaxLength() {
        SolverConfig cfg = getConfig();
        VrpSchedule problem = getInputProblem();

        Solver solver = cfg.buildSolver();
        solver.setPlanningProblem(problem);
        solver.solve(); // should be looping
    }

    private VrpSchedule getInputProblem() {
        SolutionDao solutionDao = new VehicleRoutingDao();
        return (VrpSchedule) solutionDao.readSolution(new File(solutionDao.getDataDir(), DATASET));
    }

    private SolverConfig getConfig() {
        SolverFactory sf = new XmlSolverFactory(SOLVER_CONFIG);
        return sf.getSolverConfig();
    }
}
