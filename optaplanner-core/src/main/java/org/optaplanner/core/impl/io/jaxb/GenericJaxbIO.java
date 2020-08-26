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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.optaplanner.core.impl.io.OptaPlannerXmlSerializationException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public final class GenericJaxbIO<T> implements JaxbIO<T> {
    private static final int DEFAULT_INDENTATION = 2;

    private static final String ERR_MSG_WRITE = "Unable to write the %s to XML.";
    private static final String ERR_MSG_READ = "Unable to read the (%s) from XML.";

    private final JAXBContext jaxbContext;
    private final Marshaller marshaller;
    private final Class<T> rootClass;
    private final int indentation;

    public GenericJaxbIO(Class<T> rootClass) {
        this(rootClass, DEFAULT_INDENTATION);
    }

    public GenericJaxbIO(Class<T> rootClass, int indentation) {
        Objects.requireNonNull(rootClass);
        this.rootClass = rootClass;
        this.indentation = indentation;
        try {
            jaxbContext = JAXBContext.newInstance(rootClass);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());
        } catch (JAXBException jaxbException) {
            String errMessage = String.format("Unable to create JAXB Marshaller for a root element class (%s).",
                    rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
        }
    }

    @Override
    public T read(Reader reader) {
        Objects.requireNonNull(reader);
        try {
            return (T) createUnmarshaller().unmarshal(reader);
        } catch (JAXBException jaxbException) {
            String errMessage = String.format(ERR_MSG_READ, rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
        }
    }

    public T readAndValidate(Reader reader, String schemaResource) {
        Objects.requireNonNull(reader);
        String nonNullSchemaResource = Objects.requireNonNull(schemaResource);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;
        try {
            schema = schemaFactory.newSchema(GenericJaxbIO.class.getResource(nonNullSchemaResource));
        } catch (SAXException e) {
            throw new IllegalArgumentException("Unable to read input schema resource (" + nonNullSchemaResource + ")", e);
        }

        Unmarshaller unmarshaller = createUnmarshaller();
        unmarshaller.setSchema(schema);
        ValidationEventStringCollector validationEventHandler = new ValidationEventStringCollector();

        try {
            unmarshaller.setEventHandler(validationEventHandler);
        } catch (JAXBException jaxbException) {
            String errMessage = String.format("Unable to set validation event handler to the unmarshaller for "
                    + "a root element class (%s).", rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
        }

        try {
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException jaxbException) {
            String errMessage = String.format(ERR_MSG_READ, rootClass.getName());
            if (validationEventHandler.hasEvents()) {
                String errMessageWithValidationEvents = errMessage + "\n" + validationEventHandler.reportAll();
                throw new OptaPlannerXmlSerializationException(errMessageWithValidationEvents, jaxbException);
            } else {
                throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
            }
        }
    }

    private Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            String errMessage = String.format("Unable to create JAXB unmarshaller for a root element class (%s).",
                    rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }
    }

    /**
     * Reads the input XML using the {@link Reader} overriding elements namespaces. If an element already has a namespace and
     * a {@link ElementNamespaceOverride} is defined for this element, its namespace is overridden. In case the element has no
     * namespace, new namespace defined in the {@link ElementNamespaceOverride} is added.
     * 
     * @param reader input XML {@link Reader}; never null
     * @param elementNamespaceOverrides never null
     * @return deserialized object representation of the XML.
     */
    public T readOverridingNamespace(Reader reader, ElementNamespaceOverride... elementNamespaceOverrides) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(elementNamespaceOverrides);

        final String errMessage = String.format("Unable to read the (%s) from XML with overriding elements' namespaces: %s.",
                rootClass.getName(), Arrays.toString(elementNamespaceOverrides));

        // Create a SAXParser to use its XMLReader on the XMLFilter
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            // Protect the parser against the XXE attack
            // https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A4-XML_External_Entities_(XXE)
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }
        XMLReader xmlReader;
        try {
            xmlReader = saxParser.getXMLReader();
        } catch (SAXException e) {
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }

        XMLFilter namespaceOverridingXmlFilter = new NamespaceOverridingXmlFilter(xmlReader, elementNamespaceOverrides);
        namespaceOverridingXmlFilter.setParent(xmlReader);

        // Use UnmarshallerHandler as a content handler for the XML filter.
        Unmarshaller unmarshaller = createUnmarshaller();
        UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
        namespaceOverridingXmlFilter.setContentHandler(unmarshallerHandler);

        InputSource xmlInputSource = new InputSource(reader);
        try {
            // Parse the XML to feed its content into the UnmarshallerHandler.
            namespaceOverridingXmlFilter.parse(xmlInputSource);
        } catch (IOException | SAXException e) {
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }

        try {
            return (T) unmarshallerHandler.getResult();
        } catch (JAXBException e) {
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }
    }

    @Override
    public void write(T root, Writer writer) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(writer);
        DOMResult domResult = new DOMResult();
        try {
            marshaller.marshal(root, domResult);
        } catch (JAXBException jaxbException) {
            String errMessage = String.format(ERR_MSG_WRITE, rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
        }

        formatXml(new DOMSource(domResult.getNode()), null, writer);
    }

    public void writeWithoutNamespaces(T root, Writer writer) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(writer);
        DOMResult domResult = new DOMResult();
        final String errMessage = String.format(ERR_MSG_WRITE, rootClass.getName());
        try {
            marshaller.marshal(root, domResult);
        } catch (JAXBException jaxbException) {
            throw new OptaPlannerXmlSerializationException(errMessage, jaxbException);
        }

        try (InputStream xsltInputStream = getClass().getResourceAsStream("removeNamespaces.xslt")) {
            formatXml(new DOMSource(domResult.getNode()), new StreamSource(xsltInputStream), writer);
        } catch (IOException e) {
            throw new OptaPlannerXmlSerializationException(errMessage, e);
        }
    }

    private void formatXml(Source source, Source transformationTemplate, Writer writer) {
        /*
         * The code is not vulnerable to XXE-based attacks as it does not process any external XML nor XSL input.
         * Should the transformerFactory be used for such purposes, it has to be appropriately secured:
         * https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A4-XML_External_Entities_(XXE)
         */
        @SuppressWarnings({ "java:S2755", "java:S4435" })
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformationTemplate == null ? transformerFactory.newTransformer()
                    : transformerFactory.newTransformer(transformationTemplate);
            // See https://stackoverflow.com/questions/46708498/jaxb-marshaller-indentation.
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentation));
            transformer.transform(source, new StreamResult(writer));
        } catch (TransformerException transformerException) {
            String errMessage = String.format(ERR_MSG_WRITE, rootClass.getName());
            throw new OptaPlannerXmlSerializationException(errMessage, transformerException);
        }
    }

    /**
     * Overrides namespace of every XML element by the namespace defined in the {@link ElementNamespaceOverride}.
     */
    private static final class NamespaceOverridingXmlFilter extends XMLFilterImpl {

        private final Deque<String> activeNamespace = new ArrayDeque<>();
        private final Map<String, String> elementNamespaceOverridesMap = new HashMap<>();

        public NamespaceOverridingXmlFilter(XMLReader xmlReader, ElementNamespaceOverride... elementNamespaceOverrides) {
            super(xmlReader);
            Objects.requireNonNull(elementNamespaceOverrides);
            for (ElementNamespaceOverride namespaceOverride : elementNamespaceOverrides) {
                elementNamespaceOverridesMap.put(namespaceOverride.getElementLocalName(),
                        namespaceOverride.getNamespaceOverride());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String resultingUri = activeNamespace.isEmpty() ? uri : activeNamespace.peek();
            if (elementNamespaceOverridesMap.containsKey(qName)) {
                activeNamespace.pop();
            }
            super.endElement(resultingUri, localName, qName);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            String namespaceOverride = elementNamespaceOverridesMap.get(qName);
            if (namespaceOverride != null) {
                activeNamespace.push(namespaceOverride);
            }

            String resultingUri = activeNamespace.isEmpty() ? uri : activeNamespace.peek();
            super.startElement(resultingUri, localName, qName, atts);
        }

    }

    private static final class ValidationEventStringCollector extends ValidationEventCollector {

        /**
         * Reports all validation events in a single string.
         *
         * @return string containing all validation events separated by {@link Character#LINE_SEPARATOR}
         */
        public String reportAll() {
            final StringBuilder validationEvents = new StringBuilder();

            for (ValidationEvent event : this.getEvents()) {
                validationEvents
                        .append(event.getMessage())
                        .append(Character.LINE_SEPARATOR);
            }
            return validationEvents.toString();
        }
    }
}
