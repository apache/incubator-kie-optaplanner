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

package org.optaplanner.examples.curriculumcourse.app;

import org.optaplanner.examples.common.app.CommonBenchmarkApp;

public class CurriculumCourseBenchmarkApp extends CommonBenchmarkApp {

    public static final String DEFAULT_BENCHMARK_CONFIG
            = "/org/optaplanner/examples/curriculumcourse/benchmark/curriculumCourseBenchmarkConfig.xml";
    public static final String STEP_LIMIT_BENCHMARK_CONFIG
            = "/org/optaplanner/examples/curriculumcourse/benchmark/curriculumCourseStepLimitBenchmarkConfig.xml";
    public static final String TEMPLATE_BENCHMARK_CONFIG_TEMPLATE
            = "/org/optaplanner/examples/curriculumcourse/benchmark/curriculumCourseBenchmarkConfigTemplate.xml.ftl";

    public static void main(String[] args) {
        String benchmarkConfig;
        if (args.length > 0) {
            if (args[0].equals("default")) {
                benchmarkConfig = DEFAULT_BENCHMARK_CONFIG;
            } else if (args[0].equals("stepLimit")) {
                benchmarkConfig = STEP_LIMIT_BENCHMARK_CONFIG;
            } else if (args[0].equals("template")) {
                benchmarkConfig = TEMPLATE_BENCHMARK_CONFIG_TEMPLATE;
                new CurriculumCourseBenchmarkApp().buildFromTemplateAndBenchmark(benchmarkConfig);
                return;
            } else {
                throw new IllegalArgumentException("The program argument (" + args[0] + ") is not supported.");
            }
        } else {
            benchmarkConfig = DEFAULT_BENCHMARK_CONFIG;
        }
        new CurriculumCourseBenchmarkApp().buildAndBenchmark(benchmarkConfig);
    }

}
