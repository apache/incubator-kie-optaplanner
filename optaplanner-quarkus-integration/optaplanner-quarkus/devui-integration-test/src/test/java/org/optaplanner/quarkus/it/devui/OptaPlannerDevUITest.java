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

package org.optaplanner.quarkus.it.devui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xml.sax.SAXException;

import groovy.util.Node;
import groovy.xml.XmlParser;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;

public class OptaPlannerDevUITest {

    @RegisterExtension
    static final QuarkusDevModeTest config = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addPackages(true, "org.optaplanner.quarkus.it.devui"));

    // Use the Quarkus 3 context root by default as the Quarkus platform does not pass the system property.
    static final String OPTAPLANNER_DEV_UI_BASE_URL =
            System.getProperty("dev.iu.root", "/q/dev-v1") + "/org.optaplanner.optaplanner-quarkus/";

    public static String getPage(String pageName) {
        return OPTAPLANNER_DEV_UI_BASE_URL + pageName;
    }

    @Test
    void testSolverConfigPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("solverConfig"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String solverConfig = Objects.requireNonNull(findById("optaplanner-solver-config", node)).text();
        assertThat(solverConfig).isEqualToIgnoringWhitespace(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<!--Properties that can be set at runtime are not included-->\n"
                        + "<solver>\n"
                        + "  <solutionClass>org.optaplanner.quarkus.it.devui.domain.TestdataStringLengthShadowSolution</solutionClass>\n"
                        + "  <entityClass>org.optaplanner.quarkus.it.devui.domain.TestdataStringLengthShadowEntity</entityClass>\n"
                        + "  <domainAccessType>GIZMO</domainAccessType>\n"
                        + "  <scoreDirectorFactory>\n"
                        + "    <constraintProviderClass>org.optaplanner.quarkus.it.devui.solver.TestdataStringLengthConstraintProvider</constraintProviderClass>\n"
                        + "  </scoreDirectorFactory>\n"
                        + "</solver>");
    }

    @Test
    void testModelPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("model"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String model = Objects.requireNonNull(findById("optaplanner-model", node)).toString();
        assertThat(model)
                .contains("value=[Solution: org.optaplanner.quarkus.it.devui.domain.TestdataStringLengthShadowSolution]");
        assertThat(model).contains("value=[Entity: org.optaplanner.quarkus.it.devui.domain.TestdataStringLengthShadowEntity]");
        assertThat(model).contains(
                "value=[Genuine Variables]]]]]], tbody[attributes={}; value=[tr[attributes={}; value=[td[attributes={colspan=1, rowspan=1}; value=[value]]");
        assertThat(model).contains(
                "value=[Shadow Variables]]]]]], tbody[attributes={}; value=[tr[attributes={}; value=[td[attributes={colspan=1, rowspan=1}; value=[length]]");
    }

    @Test
    void testConstraintsPage() throws ParserConfigurationException, SAXException, IOException {
        String body = RestAssured.get(getPage("constraints"))
                .then()
                .extract()
                .body()
                .asPrettyString();
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parseText(body);
        String constraints = Objects.requireNonNull(findById("optaplanner-constraints", node)).text();
        assertThat(constraints).contains("org.optaplanner.quarkus.it.devui.domain/Don't assign 2 entities the same value");
        assertThat(constraints).contains("org.optaplanner.quarkus.it.devui.domain/Maximize value length");
    }

    private Node findById(String id, Node node) {
        if (id.equals(node.attribute("id"))) {
            return node;
        }
        for (Object child : node.children()) {
            if (child instanceof Node) {
                Node maybeFoundNodeText = findById(id, (Node) child);
                if (maybeFoundNodeText != null) {
                    return maybeFoundNodeText;
                }
            }
        }
        return null;
    }
}
