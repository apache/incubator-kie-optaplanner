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

package org.optaplanner.persistence.xstream.api.score.buildin.hardsoftlong;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverterTest;

import com.thoughtworks.xstream.annotations.XStreamConverter;

class HardSoftLongScoreXStreamConverterTest extends AbstractScoreXStreamConverterTest {

    @Test
    void serializeAndDeserialize() {
        assertSerializeAndDeserialize(null, new TestHardSoftLongScoreWrapper(null));
        HardSoftLongScore score = HardSoftLongScore.of(1200L, 34L);
        assertSerializeAndDeserialize(score, new TestHardSoftLongScoreWrapper(score));
        score = HardSoftLongScore.ofUninitialized(-7, 1200L, 34L);
        assertSerializeAndDeserialize(score, new TestHardSoftLongScoreWrapper(score));
    }

    public static class TestHardSoftLongScoreWrapper extends TestScoreWrapper<HardSoftLongScore> {

        @XStreamConverter(HardSoftLongScoreXStreamConverter.class)
        private HardSoftLongScore score;

        public TestHardSoftLongScoreWrapper(HardSoftLongScore score) {
            this.score = score;
        }

        @Override
        public HardSoftLongScore getScore() {
            return score;
        }

    }

}
