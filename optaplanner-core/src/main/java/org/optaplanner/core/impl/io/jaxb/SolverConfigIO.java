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

package org.optaplanner.core.impl.io.jaxb;

import java.io.Reader;
import java.io.Writer;

import org.optaplanner.core.config.solver.SolverConfig;
import org.w3c.dom.Document;

public class SolverConfigIO implements JaxbIO<SolverConfig> {
    private static final String SOLVER_XSD_RESOURCE = "/solver.xsd";
    private final GenericJaxbIO<SolverConfig> genericJaxbIO = new GenericJaxbIO<>(SolverConfig.class);

    @Override
    public SolverConfig read(Reader reader) {
        Document document = genericJaxbIO.parseXml(reader);
        String rootElementNamespace = document.getDocumentElement().getNamespaceURI();
        if (SolverConfig.XML_NAMESPACE.equals(rootElementNamespace)) { // If there is the correct namespace, validate.
            return genericJaxbIO.readAndValidate(document, SOLVER_XSD_RESOURCE);
        } else if (rootElementNamespace == null || rootElementNamespace.isEmpty()) {
            // If not, add the missing namespace to maintain backward compatibility.
            return genericJaxbIO.readOverridingNamespace(document,
                    ElementNamespaceOverride.of(SolverConfig.XML_ELEMENT_NAME, SolverConfig.XML_NAMESPACE));
        } else { // If there is an unexpected namespace, fail fast.
            String errorMessage = String.format("The <%s/> element belongs to a different namespace (%s) than expected (%s).",
                    SolverConfig.XML_ELEMENT_NAME, rootElementNamespace, SolverConfig.XML_NAMESPACE);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    public void write(SolverConfig solverConfig, Writer writer) {
        genericJaxbIO.writeWithoutNamespaces(solverConfig, writer);
    }
}
