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

package org.optaplanner.examples.common.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.examples.common.business.SolutionBusiness;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class AbstractXmlSolutionImporter<Solution_> extends AbstractSolutionImporter<Solution_> {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = "xml";

    @Override
    public String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    public abstract XmlInputBuilder<Solution_> createXmlInputBuilder();

    @Override
    public Solution_ readSolution(File inputFile) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
            // CVE-2021-33813
            SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
            builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            builder.setExpandEntities(false);

            Document document = builder.build(in);
            XmlInputBuilder<Solution_> xmlInputBuilder = createXmlInputBuilder();
            xmlInputBuilder.setInputFile(inputFile);
            xmlInputBuilder.setDocument(document);
            try {
                Solution_ solution = xmlInputBuilder.readSolution();
                logger.info("Imported: {}", inputFile);
                return solution;
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new IllegalArgumentException("Exception in inputFile (" + inputFile + ")", e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file (" + inputFile.getName() + ").", e);
        } catch (JDOMException e) {
            throw new IllegalArgumentException("Could not parse the XML file (" + inputFile.getName() + ").", e);
        }
    }

    public static abstract class XmlInputBuilder<Solution_> extends InputBuilder {

        protected File inputFile;
        protected Document document;

        public void setInputFile(File inputFile) {
            this.inputFile = inputFile;
        }

        public void setDocument(Document document) {
            this.document = document;
        }

        public abstract Solution_ readSolution() throws IOException, JDOMException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

        public String getInputId() {
            return SolutionBusiness.getBaseFileName(inputFile);
        }

        protected void assertElementName(Element element, String name) {
            if (!element.getName().equals(name)) {
                throw new IllegalStateException("Element name (" + element.getName()
                        + ") should be " + name + ".");
            }
        }

    }

}
