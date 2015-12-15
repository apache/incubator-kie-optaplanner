/*
 * Copyright 2014 JBoss Inc
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

package org.optaplanner.examples.common.app;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.common.persistence.SolutionDao;

public abstract class ConstructionHeuristicTest extends PhaseTest {

    protected static Collection<Object[]> buildParameters(SolutionDao solutionDao, String... unsolvedFileNames) {
        return buildParameters(solutionDao, ConstructionHeuristicType.values(),
                unsolvedFileNames);
    }

    protected ConstructionHeuristicType constructionHeuristicType;

    protected ConstructionHeuristicTest(File dataFile,
            ConstructionHeuristicType constructionHeuristicType) {
        super(dataFile);
        this.constructionHeuristicType = constructionHeuristicType;
    }

    protected SolverFactory buildSolverFactory() {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(createSolverConfigResource());
        SolverConfig solverConfig = solverFactory.getSolverConfig();
        solverConfig.setTerminationConfig(new TerminationConfig());
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        constructionHeuristicPhaseConfig.setConstructionHeuristicType(constructionHeuristicType);
        solverConfig.setPhaseConfigList(Arrays.<PhaseConfig>asList(constructionHeuristicPhaseConfig));
        return solverFactory;
    }

}
