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

package org.optaplanner.persistence.jaxb.api.score.buildin.simplelong;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.persistence.jaxb.api.score.AbstractScoreJaxbAdapterTest;

class SimpleLongScoreJaxbAdapterTest extends AbstractScoreJaxbAdapterTest {

    @Test
    void serializeAndDeserialize() {
        assertSerializeAndDeserialize(null, new TestSimpleLongScoreWrapper(null));

        SimpleLongScore score = SimpleLongScore.of(1234L);
        assertSerializeAndDeserialize(score, new TestSimpleLongScoreWrapper(score));

        score = SimpleLongScore.ofUninitialized(-7, 1234L);
        assertSerializeAndDeserialize(score, new TestSimpleLongScoreWrapper(score));
    }

    @XmlRootElement
    public static class TestSimpleLongScoreWrapper extends AbstractScoreJaxbAdapterTest.TestScoreWrapper<SimpleLongScore> {

        @XmlJavaTypeAdapter(SimpleLongScoreJaxbAdapter.class)
        private SimpleLongScore score;

        @SuppressWarnings("unused")
        private TestSimpleLongScoreWrapper() {
        }

        public TestSimpleLongScoreWrapper(SimpleLongScore score) {
            this.score = score;
        }

        @Override
        public SimpleLongScore getScore() {
            return score;
        }

    }

}
