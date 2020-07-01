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

package org.optaplanner.persistence.jaxb.api.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.optaplanner.core.api.score.Score;

public abstract class AbstractScoreJaxbAdapterTest {

    // ************************************************************************
    // Helper methods
    // ************************************************************************

    protected <S extends Score, W extends TestScoreWrapper<S>> void assertSerializeAndDeserialize(S expectedScore, W input) {
        assertSerializeAndDeserializeXML(expectedScore, input);
        assertSerializeAndDeserializeJson(expectedScore, input);
    }

    protected <S extends Score, W extends TestScoreWrapper<S>> void assertSerializeAndDeserializeXML(S expectedScore, W input) {
        String xmlString;
        W output;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(input.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();
            jaxbMarshaller.marshal(input, writer);
            xmlString = writer.toString();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlString);
            output = (W) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new IllegalStateException("Marshalling or unmarshalling for input (" + input + ") failed.", e);
        }
        assertThat(output.getScore()).isEqualTo(expectedScore);
        String regex;
        if (expectedScore != null) {
            regex = "<\\?[^\\?]*\\?>" // XML header
                    + "<([\\w\\-\\.]+)>\\s*" // Start of element
                    + "<score>"
                    + expectedScore.toString().replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]") // Score
                    + "</score>"
                    + "\\s*</\\1>"; // End of element
        } else {
            regex = "<\\?[^\\?]*\\?>" // XML header
                    + "<([\\w\\-\\.]+)/>"; // Start and end of element
        }
        if (!xmlString.matches(regex)) {
            fail(String.format("Regular expression match failed.%nExpected regular expression: %s%n" +
                    "Actual string: %s", regex, xmlString));
        }
    }

    protected <S extends Score, W extends TestScoreWrapper<S>> void assertSerializeAndDeserializeJson(S expectedScore,
            W input) {
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        String jsonString;
        W output;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(input.getClass());

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            jaxbMarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            jaxbMarshaller.marshal(input, writer);
            jsonString = writer.toString();

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);

            StringReader reader = new StringReader(jsonString);
            output = (W) unmarshaller.unmarshal(reader);

            System.clearProperty("javax.xml.bind.context.factory");
        } catch (JAXBException e) {
            throw new IllegalStateException("Marshalling or unmarshalling for input (" + input + ") failed.", e);
        }
        assertThat(output.getScore()).isEqualTo(expectedScore);
        String regex;
        if (expectedScore != null) {
            regex = "\\{\\R" // Opening bracket
                    + "\\s*\"([\\w]+)\"\\s:\\s\\{\\R" // Start of element
                    + "\\s*\"score\"\\s:\\s\"" // Start of element
                    + expectedScore.toString().replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]") // Score
                    + "\"\\s*\\}\\R" // End of element
                    + "\\}"; // Closing bracket
        } else {
            regex = "\\{\\R" // Opening bracket
                    + "\\s*\"([\\w]+)\"\\s:\\s\\{\\R" // Start of element
                    + "\\s*\\}\\R" // End of element
                    + "\\}"; // Closing bracket
        }
        if (!jsonString.matches(regex)) {
            fail(String.format("Regular expression match failed.%nExpected regular expression: %s%n" +
                    "Actual string: %s", regex, jsonString));
        }
    }

    public static abstract class TestScoreWrapper<S extends Score> {

        public abstract S getScore();

    }

}
