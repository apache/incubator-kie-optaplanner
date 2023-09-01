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

package org.optaplanner.core.impl.score.buildin;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;

class HardMediumSoftBigDecimalScoreDefinitionTest {

    @Test
    void getZeroScore() {
        HardMediumSoftBigDecimalScore score = new HardMediumSoftBigDecimalScoreDefinition().getZeroScore();
        assertThat(score).isEqualTo(HardMediumSoftBigDecimalScore.ZERO);
    }

    @Test
    void getSoftestOneScore() {
        HardMediumSoftBigDecimalScore score = new HardMediumSoftBigDecimalScoreDefinition().getOneSoftestScore();
        assertThat(score).isEqualTo(HardMediumSoftBigDecimalScore.ONE_SOFT);
    }

    @Test
    void getLevelsSize() {
        assertThat(new HardMediumSoftBigDecimalScoreDefinition().getLevelsSize()).isEqualTo(3);
    }

    @Test
    void getLevelLabels() {
        assertThat(new HardMediumSoftBigDecimalScoreDefinition().getLevelLabels())
                .containsExactly("hard score", "medium score", "soft score");
    }

    @Test
    void getFeasibleLevelsSize() {
        assertThat(new HardMediumSoftBigDecimalScoreDefinition().getFeasibleLevelsSize()).isEqualTo(1);
    }

    // Optimistic and pessimistic bounds are currently not supported for this score definition

    @Test
    void divideBySanitizedDivisor() {
        HardMediumSoftBigDecimalScoreDefinition scoreDefinition = new HardMediumSoftBigDecimalScoreDefinition();
        HardMediumSoftBigDecimalScore dividend = scoreDefinition.fromLevelNumbers(2,
                new Number[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN });
        HardMediumSoftBigDecimalScore zeroDivisor = scoreDefinition.getZeroScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, zeroDivisor))
                .isEqualTo(dividend);
        HardMediumSoftBigDecimalScore oneDivisor = scoreDefinition.getOneSoftestScore();
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, oneDivisor))
                .isEqualTo(dividend);
        HardMediumSoftBigDecimalScore tenDivisor = scoreDefinition.fromLevelNumbers(10,
                new Number[] { BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN });
        assertThat(scoreDefinition.divideBySanitizedDivisor(dividend, tenDivisor))
                .isEqualTo(scoreDefinition.fromLevelNumbers(0,
                        new Number[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE }));
    }

}
