/*
 * Copyright 2010 JBoss Inc
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

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.benchmark.config.FreemarkerXmlPlannerBenchmarkFactory;
import org.optaplanner.benchmark.config.XmlPlannerBenchmarkFactory;

public abstract class CommonBenchmarkApp extends LoggingMain {

    public void buildAndBenchmark(String benchmarkConfig) {
        PlannerBenchmarkFactory plannerBenchmarkFactory = new XmlPlannerBenchmarkFactory(benchmarkConfig);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();
    }

    public void buildFromTemplateAndBenchmark(String benchmarkConfigTemplate) {
        PlannerBenchmarkFactory plannerBenchmarkFactory = new FreemarkerXmlPlannerBenchmarkFactory(
                benchmarkConfigTemplate);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();
    }

    public void buildAndResumeBenchmark(String benchmarkConfig, String resumeFilePath) {
        PlannerBenchmarkFactory plannerBenchmarkFactory = new XmlPlannerBenchmarkFactory(benchmarkConfig);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildResumableBenchmark(resumeFilePath);
        plannerBenchmark.benchmark();
    }
}
