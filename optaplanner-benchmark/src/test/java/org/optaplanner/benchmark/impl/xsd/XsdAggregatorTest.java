/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.benchmark.impl.xsd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;
import org.optaplanner.core.impl.io.jaxb.GenericJaxbIO;
import org.xml.sax.SAXException;

class XsdAggregatorTest {

    private static final String VALID_BENCHMARK_CONFIG_XML = "validBenchmarkConfig.xml";

    @Test
    void validateByMergedXsd() throws IOException {
        // 1. Use the solver.xsd and the benchmark-with-import.xsd from the classpath.
        File solverXsd = getResourceAsFile("/solver.xsd");
        File benchmarkXsd = getResourceAsFile("/benchmark-with-import.xsd");
        File mergedBenchmarkXsd = File.createTempFile("benchmark_", ".xsd");

        // 2. Merge them to a single file.
        String[] args = new String[] { solverXsd.getAbsolutePath(), benchmarkXsd.getAbsolutePath(),
                mergedBenchmarkXsd.getAbsolutePath() };
        XsdAggregator.main(args);

        // 3. prepare a short benchmarkConfig (including solver config) and validate it
        GenericJaxbIO<PlannerBenchmarkConfig> jaxbIO = new GenericJaxbIO<>(PlannerBenchmarkConfig.class);
        PlannerBenchmarkConfig plannerBenchmarkConfig;
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(VALID_BENCHMARK_CONFIG_XML))) {
            plannerBenchmarkConfig = jaxbIO.readAndValidate(reader, readXmlSchemaFromFile(mergedBenchmarkXsd));
        }

        assertThat(plannerBenchmarkConfig).isNotNull();
    }

    private File getResourceAsFile(String classPathResource) {
        try {
            URL resourceURl = getClass().getResource(classPathResource);
            return new File(resourceURl.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Class path resource " + classPathResource + " is not a valid URI.", e);
        }
    }

    private Schema readXmlSchemaFromFile(File schemaFile) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return schemaFactory.newSchema(schemaFile);
        } catch (SAXException e) {
            throw new IllegalArgumentException(String.format("Failed to read schema from a file (%s).", schemaFile), e);
        }
    }
}
