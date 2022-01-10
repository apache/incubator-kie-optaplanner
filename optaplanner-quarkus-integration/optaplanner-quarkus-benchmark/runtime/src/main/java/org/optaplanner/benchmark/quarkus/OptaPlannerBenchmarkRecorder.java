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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;
import org.optaplanner.benchmark.config.ProblemBenchmarksConfig;
import org.optaplanner.benchmark.config.SolverBenchmarkConfig;
import org.optaplanner.benchmark.quarkus.config.OptaPlannerBenchmarkRuntimeConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class OptaPlannerBenchmarkRecorder {
    public Supplier<PlannerBenchmarkConfig> benchmarkConfigSupplier(PlannerBenchmarkConfig benchmarkConfig) {
        return () -> {
            OptaPlannerBenchmarkRuntimeConfig optaPlannerRuntimeConfig =
                    Arc.container().instance(OptaPlannerBenchmarkRuntimeConfig.class).get();
            SolverConfig solverConfig =
                    Arc.container().instance(SolverConfig.class).get();
            updateBenchmarkConfigWithRuntimeProperties(benchmarkConfig, optaPlannerRuntimeConfig, solverConfig);
            return benchmarkConfig;
        };
    }

    private void updateBenchmarkConfigWithRuntimeProperties(PlannerBenchmarkConfig plannerBenchmarkConfig,
            OptaPlannerBenchmarkRuntimeConfig benchmarkRuntimeConfig,
            SolverConfig solverConfig) {
        if (plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig() == null) {
            ProblemBenchmarksConfig problemBenchmarksConfig = new ProblemBenchmarksConfig();
            SolverBenchmarkConfig solverBenchmarkConfig = new SolverBenchmarkConfig();
            SolverConfig benchmarkSolverConfig = new SolverConfig();
            benchmarkSolverConfig.inherit(solverConfig);

            solverBenchmarkConfig.setSolverConfig(benchmarkSolverConfig);
            solverBenchmarkConfig.setProblemBenchmarksConfig(problemBenchmarksConfig);

            plannerBenchmarkConfig.setBenchmarkDirectory(new File(benchmarkRuntimeConfig.resultDirectory));
            plannerBenchmarkConfig.setInheritedSolverBenchmarkConfig(solverBenchmarkConfig);
        }

        TerminationConfig terminationConfig = plannerBenchmarkConfig.getInheritedSolverBenchmarkConfig()
                .getSolverConfig().getTerminationConfig();
        benchmarkRuntimeConfig.termination.spentLimit.ifPresent(terminationConfig::setSpentLimit);
        benchmarkRuntimeConfig.termination.unimprovedSpentLimit
                .ifPresent(terminationConfig::setUnimprovedSpentLimit);
        benchmarkRuntimeConfig.termination.bestScoreLimit.ifPresent(terminationConfig::setBestScoreLimit);

        if (!isTerminationConfigured(terminationConfig)) {
            List<SolverBenchmarkConfig> solverBenchmarkConfigList = plannerBenchmarkConfig.getSolverBenchmarkConfigList();
            List<String> unconfiguredTerminationSolverBenchmarkList = new ArrayList<>();
            if (solverBenchmarkConfigList == null) {
                throw new IllegalStateException("At least one of the properties " +
                        "quarkus.optaplanner.benchmark.solver.termination.spent-limit, " +
                        "quarkus.optaplanner.benchmark.solver.termination.best-score-limit, " +
                        "quarkus.optaplanner.benchmark.solver.termination.unimproved-spent-limit " +
                        "is required if termination is not configured in the " +
                        "inherited solver benchmark config and solverBenchmarkBluePrint is used.");
            }
            for (int i = 0; i < solverBenchmarkConfigList.size(); i++) {
                SolverBenchmarkConfig solverBenchmarkConfig = solverBenchmarkConfigList.get(i);
                terminationConfig = solverBenchmarkConfig.getSolverConfig().getTerminationConfig();
                if (!isTerminationConfigured(terminationConfig)) {
                    boolean isTerminationConfiguredForAllNonConstructionHeuristicPhases = !solverBenchmarkConfig
                            .getSolverConfig().getPhaseConfigList().isEmpty();
                    for (PhaseConfig<?> phaseConfig : solverBenchmarkConfig.getSolverConfig().getPhaseConfigList()) {
                        if (!(phaseConfig instanceof ConstructionHeuristicPhaseConfig)) {
                            if (!isTerminationConfigured(phaseConfig.getTerminationConfig())) {
                                System.out.println("The phase " + phaseConfig + " is not configured.");
                                isTerminationConfiguredForAllNonConstructionHeuristicPhases = false;
                                break;
                            }
                        }
                    }
                    if (!isTerminationConfiguredForAllNonConstructionHeuristicPhases) {
                        String benchmarkConfigName = solverBenchmarkConfig.getName();
                        if (benchmarkConfigName == null) {
                            benchmarkConfigName = "SolverBenchmarkConfig " + i;
                        }
                        unconfiguredTerminationSolverBenchmarkList.add(benchmarkConfigName);
                    }
                }
            }
            if (!unconfiguredTerminationSolverBenchmarkList.isEmpty()) {
                throw new IllegalStateException("The following " + SolverBenchmarkConfig.class.getSimpleName() + " do not " +
                        "have termination configured: " +
                        String.join(", ", unconfiguredTerminationSolverBenchmarkList) + ". " +
                        "At least one of the properties " +
                        "quarkus.optaplanner.benchmark.solver.termination.spent-limit, " +
                        "quarkus.optaplanner.benchmark.solver.termination.best-score-limit, " +
                        "quarkus.optaplanner.benchmark.solver.termination.unimproved-spent-limit " +
                        "is required if termination is not configured in a solver benchmark and the " +
                        "inherited solver benchmark config.");
            }
        }

        if (plannerBenchmarkConfig.getSolverBenchmarkConfigList() == null
                && plannerBenchmarkConfig.getSolverBenchmarkBluePrintConfigList() == null) {
            plannerBenchmarkConfig.setSolverBenchmarkConfigList(Collections.singletonList(new SolverBenchmarkConfig()));
        }
    }

    private boolean isTerminationConfigured(TerminationConfig terminationConfig) {
        if (terminationConfig == null) {
            return false;
        }
        return terminationConfig.getTerminationClass() != null ||
                terminationConfig.getSpentLimit() != null ||
                terminationConfig.getMillisecondsSpentLimit() != null ||
                terminationConfig.getSecondsSpentLimit() != null ||
                terminationConfig.getMinutesSpentLimit() != null ||
                terminationConfig.getHoursSpentLimit() != null ||
                terminationConfig.getDaysSpentLimit() != null ||
                terminationConfig.getBestScoreLimit() != null ||
                terminationConfig.getUnimprovedSpentLimit() != null ||
                terminationConfig.getUnimprovedMillisecondsSpentLimit() != null ||
                terminationConfig.getUnimprovedSecondsSpentLimit() != null ||
                terminationConfig.getUnimprovedMinutesSpentLimit() != null ||
                terminationConfig.getUnimprovedHoursSpentLimit() != null ||
                terminationConfig.getUnimprovedDaysSpentLimit() != null ||
                terminationConfig.getStepCountLimit() != null ||
                terminationConfig.getTerminationConfigList() != null;
    }
}
