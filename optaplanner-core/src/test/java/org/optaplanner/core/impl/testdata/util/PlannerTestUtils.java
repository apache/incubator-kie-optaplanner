/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.testdata.util;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.SerializationUtils;
import org.mockito.AdditionalAnswers;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.score.director.easy.EasyScoreDirectorFactory;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

import java.io.Serializable;

import static org.mockito.Mockito.mock;

public class PlannerTestUtils {

    // ************************************************************************
    // ScoreDirector methods
    // ************************************************************************

    public static <Solution_> InnerScoreDirector mockScoreDirector(SolutionDescriptor<Solution_> solutionDescriptor) {
        EasyScoreDirectorFactory<Solution_> scoreDirectorFactory =
                new EasyScoreDirectorFactory<>((EasyScoreCalculator<Solution_>) solution -> SimpleScore.valueOf(0));
        scoreDirectorFactory.setSolutionDescriptor(solutionDescriptor);
        scoreDirectorFactory.setScoreDefinition(new SimpleScoreDefinition());
        scoreDirectorFactory.setInitializingScoreTrend(
                InitializingScoreTrend.buildUniformTrend(InitializingScoreTrendLevel.ONLY_DOWN, 1));
        return mock(InnerScoreDirector.class, AdditionalAnswers.delegatesTo(scoreDirectorFactory.buildScoreDirector(false)));
    }

    // ************************************************************************
    // Serialization methods
    // ************************************************************************

    public static <T> void serializeAndDeserializeWithAll(T input, OutputAsserter<T> outputAsserter) {
        outputAsserter.assertOutput(serializeAndDeserializeWithJavaSerialization(input));
        outputAsserter.assertOutput(serializeAndDeserializeWithXStream(input));
    }

    public static <T> T serializeAndDeserializeWithJavaSerialization(T input) {
        byte[] bytes = SerializationUtils.serialize((Serializable) input);
        return (T) SerializationUtils.deserialize(bytes);
    }

    public static <T> T serializeAndDeserializeWithXStream(T input) {
        XStream xStream = new XStream();
        xStream.setMode(XStream.ID_REFERENCES);
        if (input != null) {
            xStream.processAnnotations(input.getClass());
        }
        String xmlString = xStream.toXML(input);
        return (T) xStream.fromXML(xmlString);
    }

    public static interface OutputAsserter<T> {

        void assertOutput(T output);

    }

    // ************************************************************************
    // Private constructor
    // ************************************************************************

    private PlannerTestUtils() {
    }

}
