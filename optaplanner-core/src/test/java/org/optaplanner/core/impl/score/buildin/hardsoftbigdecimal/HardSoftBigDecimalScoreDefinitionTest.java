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

package org.optaplanner.core.impl.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HardSoftBigDecimalScoreDefinitionTest {

    @Test
    public void getZeroScore() {
        HardSoftBigDecimalScore score = new HardSoftBigDecimalScoreDefinition().getZeroScore();
        assertThat(score).isEqualTo(HardSoftBigDecimalScore.ZERO);
    }

    @Test
    public void getSoftestOneScore() {
        HardSoftBigDecimalScore score = new HardSoftBigDecimalScoreDefinition().getOneSoftestScore();
        assertThat(score).isEqualTo(HardSoftBigDecimalScore.ONE_SOFT);
    }

    @Test
    public void getLevelsSize() {
        assertEquals(2, new HardSoftBigDecimalScoreDefinition().getLevelsSize());
    }

    @Test
    public void getLevelLabels() {
        assertArrayEquals(new String[]{"hard score", "soft score"}, new HardSoftBigDecimalScoreDefinition().getLevelLabels());
    }

    @Test
    public void getFeasibleLevelsSize() {
        assertEquals(1, new HardSoftBigDecimalScoreDefinition().getFeasibleLevelsSize());
    }

    // Optimistic and pessimistic bounds are currently not supported for this score definition

    @Test
    public void divideBySanitizedDivisor() {
        HardSoftBigDecimalScoreDefinition scoreDefinition = new HardSoftBigDecimalScoreDefinition();
        HardSoftBigDecimalScore dividend = scoreDefinition.fromLevelNumbers(2,
                new Number[] {BigDecimal.ZERO, BigDecimal.TEN});
        HardSoftBigDecimalScore zeroDivisor = scoreDefinition.getZeroScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, zeroDivisor))
                .isEqualTo(dividend);
        HardSoftBigDecimalScore oneDivisor = scoreDefinition.getOneSoftestScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, oneDivisor))
                .isEqualTo(dividend);
        HardSoftBigDecimalScore tenDivisor = scoreDefinition.fromLevelNumbers(10,
                new Number[] {BigDecimal.TEN, BigDecimal.TEN});
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, tenDivisor))
                .isEqualTo(scoreDefinition.fromLevelNumbers(0,
                        new Number[] {BigDecimal.ZERO, BigDecimal.ONE}));
    }

}
