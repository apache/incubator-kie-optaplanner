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

import java.time.Duration;

import org.optaplanner.core.config.solver.termination.TerminationConfig;

import io.quarkus.runtime.annotations.ConfigGroup;

/**
 * During build time, this is translated into OptaPlanner's {@link TerminationConfig}.
 */
@ConfigGroup
public class TerminationBuildTimeConfig {

    /**
     * How long solver should be run in a benchmark run.
     * For example: "30s" is 30 seconds. "5m" is 5 minutes. "2h" is 2 hours. "1d" is 1 day.
     * Also supports ISO-8601 format, see {@link Duration}.
     */
    public Duration spentLimit;

}
