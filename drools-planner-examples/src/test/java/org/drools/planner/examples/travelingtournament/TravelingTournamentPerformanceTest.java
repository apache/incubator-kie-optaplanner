/*
 * Copyright 2011 JBoss Inc
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

package org.drools.planner.examples.travelingtournament;

import java.io.File;

import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.examples.common.app.SolverPerformanceTest;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.travelingtournament.persistence.smart.SmartTravelingTournamentDaoImpl;
import org.junit.Test;

public class TravelingTournamentPerformanceTest extends SolverPerformanceTest {

    @Override
    protected String createSolverConfigResource() {
        return "/org/drools/planner/examples/travelingtournament/solver/smart/smartTravelingTournamentSolverConfig.xml";
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new SmartTravelingTournamentDaoImpl();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 180000)
    public void solveComp01_initialized() {
        File unsolvedDataFile = new File("data/travelingtournament/smart/unsolved/1-nl10.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-75968soft");
    }

    @Test(timeout = 180000)
    public void solveTestdata01_initializedDebug() {
        File unsolvedDataFile = new File("data/travelingtournament/smart/unsolved/1-nl10.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-77619soft", EnvironmentMode.DEBUG);
    }

}
