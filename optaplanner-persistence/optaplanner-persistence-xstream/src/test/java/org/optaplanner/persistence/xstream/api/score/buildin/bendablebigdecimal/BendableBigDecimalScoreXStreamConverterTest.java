/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.xstream.api.score.buildin.bendablebigdecimal;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverterTest;

public class BendableBigDecimalScoreXStreamConverterTest extends AbstractScoreXStreamConverterTest {

    @Test
    public void serializeAndDeserialize() {
        assertSerializeAndDeserialize(null, new TestBendableBigDecimalScoreWrapper(null));
        BendableBigDecimalScore score = BendableBigDecimalScore.of(
                new BigDecimal[]{new BigDecimal("1000.0001"), new BigDecimal("200.0020")}, new BigDecimal[]{new BigDecimal("34.4300")});
        assertSerializeAndDeserialize(score, new TestBendableBigDecimalScoreWrapper(score));
        score = BendableBigDecimalScore.ofUninitialized(-7,
                new BigDecimal[]{new BigDecimal("1000.0001"), new BigDecimal("200.0020")}, new BigDecimal[]{new BigDecimal("34.4300")});
        assertSerializeAndDeserialize(score, new TestBendableBigDecimalScoreWrapper(score));
    }

    public static class TestBendableBigDecimalScoreWrapper extends TestScoreWrapper<BendableBigDecimalScore> {

        @XStreamConverter(BendableBigDecimalScoreXStreamConverter.class)
        private BendableBigDecimalScore score;

        public TestBendableBigDecimalScoreWrapper(BendableBigDecimalScore score) {
            this.score = score;
        }

        @Override
        public BendableBigDecimalScore getScore() {
            return score;
        }

    }

}
