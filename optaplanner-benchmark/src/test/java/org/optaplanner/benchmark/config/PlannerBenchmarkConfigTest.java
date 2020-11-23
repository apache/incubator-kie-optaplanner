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

package org.optaplanner.benchmark.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.optaplanner.benchmark.impl.io.PlannerBenchmarkConfigIO;
import org.optaplanner.core.impl.io.OptaPlannerXmlSerializationException;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

class PlannerBenchmarkConfigTest {

    private static final String TEST_PLANNER_BENCHMARK_CONFIG_WITH_NAMESPACE = "testBenchmarkConfigWithNamespace.xml";
    private static final String TEST_PLANNER_BENCHMARK_CONFIG_WITHOUT_NAMESPACE = "testBenchmarkConfigWithoutNamespace.xml";

    @ParameterizedTest
    @ValueSource(strings = { TEST_PLANNER_BENCHMARK_CONFIG_WITHOUT_NAMESPACE, TEST_PLANNER_BENCHMARK_CONFIG_WITH_NAMESPACE })
    void xmlConfigFileRemainsSameAfterReadWrite(String xmlBenchmarkConfigResource) throws IOException {
        PlannerBenchmarkConfigIO xmlIO = new PlannerBenchmarkConfigIO();
        PlannerBenchmarkConfig jaxbBenchmarkConfig;

        try (Reader reader = new InputStreamReader(
                PlannerBenchmarkConfigTest.class.getResourceAsStream(xmlBenchmarkConfigResource))) {
            jaxbBenchmarkConfig = xmlIO.read(reader);
        }

        assertThat(jaxbBenchmarkConfig).isNotNull();

        Writer stringWriter = new StringWriter();
        xmlIO.write(jaxbBenchmarkConfig, stringWriter);
        String jaxbString = stringWriter.toString();

        String originalXml = IOUtils.toString(PlannerBenchmarkConfigTest.class.getResourceAsStream(xmlBenchmarkConfigResource),
                StandardCharsets.UTF_8);

        // During writing the benchmark config, the benchmark element's namespace is removed.
        String benchmarkElementWithNamespace =
                PlannerBenchmarkConfig.XML_ELEMENT_NAME + " xmlns=\"" + PlannerBenchmarkConfig.XML_NAMESPACE + "\"";
        if (originalXml.contains(benchmarkElementWithNamespace)) {
            originalXml = originalXml.replace(benchmarkElementWithNamespace, PlannerBenchmarkConfig.XML_ELEMENT_NAME);
        }
        assertThat(jaxbString).isXmlEqualTo(originalXml);
    }

    @Test
    void readAndValidateInvalidBenchmarkConfig_failsIndicatingTheIssue() {
        PlannerBenchmarkConfigIO xmlIO = new PlannerBenchmarkConfigIO();
        String benchmarkConfigXml = "<plannerBenchmark xmlns=\"https://www.optaplanner.org/xsd/benchmark\">\n"
                + "  <benchmarkDirectory>data</benchmarkDirectory>\n"
                + "  <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>\n"
                + "  <solverBenchmark>\n"
                + "    <name>Entity Tabu Search</name>\n"
                + "    <solver>\n"
                // Intentionally wrong to simulate a typo.
                + "      <solutionKlazz>org.optaplanner.core.impl.testdata.domain.TestdataSolution</solutionKlazz>\n"
                + "      <entityClass>org.optaplanner.core.impl.testdata.domain.TestdataEntity</entityClass>\n"
                + "    </solver>\n"
                + "    <problemBenchmarks>\n"
                + "      <solutionFileIOClass>org.optaplanner.benchmark.config.PlannerBenchmarkConfigTest$TestdataSolutionFileIO</solutionFileIOClass>\n"
                + "      <inputSolutionFile>nonExistingDataset1.xml</inputSolutionFile>\n"
                + "    </problemBenchmarks>\n"
                + "  </solverBenchmark>\n"
                + "</plannerBenchmark>\n";

        StringReader stringReader = new StringReader(benchmarkConfigXml);
        assertThatExceptionOfType(OptaPlannerXmlSerializationException.class)
                .isThrownBy(() -> xmlIO.read(stringReader))
                .withMessageContaining("Invalid content was found")
                .withMessageContaining("solutionKlazz");
    }

    // Used by the testBenchmarkConfig.xml
    private static class TestdataSolutionFileIO extends XStreamSolutionFileIO<TestdataSolution> {
        private TestdataSolutionFileIO() {
            super(TestdataSolution.class);
        }
    }
}
