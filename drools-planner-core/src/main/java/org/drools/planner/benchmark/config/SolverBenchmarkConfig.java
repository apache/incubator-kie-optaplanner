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

package org.drools.planner.benchmark.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.drools.planner.benchmark.core.PlannerBenchmarkResult;
import org.drools.planner.benchmark.core.ProblemBenchmark;
import org.drools.planner.benchmark.core.SolverBenchmark;
import org.drools.planner.config.solver.SolverConfig;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solverBenchmark")
public class SolverBenchmarkConfig {

    private String name = null;

    @XStreamAlias("solver")
    private SolverConfig solverConfig = null;

    @XStreamAlias("problemBenchmarks")
    private ProblemBenchmarksConfig problemBenchmarksConfig = new ProblemBenchmarksConfig();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SolverConfig getSolverConfig() {
        return solverConfig;
    }

    public void setSolverConfig(SolverConfig solverConfig) {
        this.solverConfig = solverConfig;
    }

    public ProblemBenchmarksConfig getProblemBenchmarksConfig() {
        return problemBenchmarksConfig;
    }

    public void setProblemBenchmarksConfig(ProblemBenchmarksConfig problemBenchmarksConfig) {
        this.problemBenchmarksConfig = problemBenchmarksConfig;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public SolverBenchmark buildSolverBenchmark(List<ProblemBenchmark> unifiedProblemBenchmarkList, ExecutorService executor) {
        SolverBenchmark solverBenchmark = new SolverBenchmark();
        solverBenchmark.setName(name);
        solverBenchmark.setSolverConfig(solverConfig);
        solverBenchmark.setPlannerBenchmarkResultList(new ArrayList<PlannerBenchmarkResult>());
        List<ProblemBenchmark> problemBenchmarkList = problemBenchmarksConfig
                .buildProblemBenchmarkList(unifiedProblemBenchmarkList, solverBenchmark, executor);
        solverBenchmark.setProblemBenchmarkList(problemBenchmarkList);
        return solverBenchmark;
    }

    public void inherit(SolverBenchmarkConfig inheritedConfig) {
        if (solverConfig == null) {
            solverConfig = inheritedConfig.getSolverConfig();
        } else if (inheritedConfig.getSolverConfig() != null) {
            solverConfig.inherit(inheritedConfig.getSolverConfig());
        }
        if (problemBenchmarksConfig == null) {
            problemBenchmarksConfig = inheritedConfig.getProblemBenchmarksConfig();
        } else if (inheritedConfig.getProblemBenchmarksConfig() != null) {
            problemBenchmarksConfig.inherit(inheritedConfig.getProblemBenchmarksConfig());
        }
    }

}
