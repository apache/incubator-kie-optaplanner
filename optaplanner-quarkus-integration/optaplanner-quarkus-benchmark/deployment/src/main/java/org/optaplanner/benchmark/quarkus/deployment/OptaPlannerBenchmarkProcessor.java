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

package org.optaplanner.benchmark.quarkus.deployment;

import org.jboss.logging.Logger;
import org.optaplanner.benchmark.quarkus.OptaPlannerBenchmarkBeanProvider;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;

class OptaPlannerBenchmarkProcessor {

    private static final Logger log = Logger.getLogger(OptaPlannerBenchmarkProcessor.class.getName());

    OptaPlannerBenchmarkBuildTimeConfig optaPlannerBuildTimeConfig;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("optaplanner-benchmark");
    }

    @BuildStep
    HotDeploymentWatchedFileBuildItem watchSolverBenchmarkConfigXml() {
        String solverConfigXML = optaPlannerBuildTimeConfig.solverBenchmarkConfigXml
                .orElse(OptaPlannerBenchmarkBuildTimeConfig.DEFAULT_SOLVER_BENCHMARK_CONFIG_URL);
        return new HotDeploymentWatchedFileBuildItem(solverConfigXML);
    }

    @BuildStep
    void registerAdditionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(new AdditionalBeanBuildItem(OptaPlannerBenchmarkBeanProvider.class));
    }

    private boolean isClassDefined(String className) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
