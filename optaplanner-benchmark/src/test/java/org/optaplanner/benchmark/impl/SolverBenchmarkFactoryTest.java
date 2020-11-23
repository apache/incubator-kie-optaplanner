/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.impl;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.config.SolverBenchmarkConfig;

public class SolverBenchmarkFactoryTest {

    @Test
    public void validNameWithUnderscoreAndSpace() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("Valid_name with space_and_underscore");
        config.setSubSingleCount(1);
        validateConfig(config);
    }

    @Test
    public void validNameWithJapanese() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("Valid name (有効名 in Japanese)");
        config.setSubSingleCount(1);
        validateConfig(config);
    }

    @Test
    public void invalidNameWithSlash() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("slash/name");
        config.setSubSingleCount(1);
        assertThatIllegalStateException().isThrownBy(() -> validateConfig(config));
    }

    @Test
    public void invalidNameWithSuffixWhitespace() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("Suffixed with space ");
        config.setSubSingleCount(1);
        assertThatIllegalStateException().isThrownBy(() -> validateConfig(config));
    }

    @Test
    public void invalidNameWithPrefixWhitespace() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName(" prefixed with space");
        config.setSubSingleCount(1);
        assertThatIllegalStateException().isThrownBy(() -> validateConfig(config));
    }

    @Test
    public void validNonZeroSubSingleCount() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("name");
        config.setSubSingleCount(2);
        validateConfig(config);
    }

    @Test
    public void validNullSubSingleCount() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("name");
        config.setSubSingleCount(null);
        validateConfig(config);
    }

    @Test
    public void invalidZeroSubSingleCount() {
        SolverBenchmarkConfig config = new SolverBenchmarkConfig();
        config.setName("name");
        config.setSubSingleCount(0);
        assertThatIllegalStateException().isThrownBy(() -> validateConfig(config));
    }

    private void validateConfig(SolverBenchmarkConfig config) {
        SolverBenchmarkFactory solverBenchmarkFactory = new SolverBenchmarkFactory(config);
        solverBenchmarkFactory.validate();
    }
}
