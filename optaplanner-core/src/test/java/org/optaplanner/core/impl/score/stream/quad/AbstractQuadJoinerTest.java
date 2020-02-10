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

package org.optaplanner.core.impl.score.stream.quad;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner.merge;

public class AbstractQuadJoinerTest {

    @Test
    public void merge0Joiners() {
        assertThat(merge()).isInstanceOf(NoneQuadJoiner.class);
    }

    @Test
    public void merge1JoinersNone() {
        assertThat(merge(new NoneQuadJoiner<>())).isInstanceOf(NoneQuadJoiner.class);
    }

    @Test
    public void merge1JoinersSingle() {
        assertThat(merge(Joiners.equal((a, b, c) -> 0, d -> 0))).isInstanceOf(SingleQuadJoiner.class);
    }

    @Test
    public void merge2Joiners() {
        QuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> joiner1 =
                Joiners.equal((a, b, c) -> a.add(b).add(c).longValue(), BigInteger::longValue);
        QuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> joiner2 =
                Joiners.lessThan((a, b, c) -> a.add(b).add(c).longValue(), BigInteger::longValue);
        AbstractQuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> mergedJoiner = merge(joiner1, joiner2);
        assertSoftly(softly -> {
            softly.assertThat(mergedJoiner).isInstanceOf(CompositeQuadJoiner.class);
            softly.assertThat(mergedJoiner.getJoinerTypes()).containsExactly(JoinerType.EQUAL, JoinerType.LESS_THAN);
            softly.assertThatThrownBy(() -> mergedJoiner.getLeftMapping(2)).isInstanceOf(IllegalArgumentException.class);
            softly.assertThatThrownBy(() -> mergedJoiner.getRightMapping(2)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void merge2Joiners1Composite() {
        QuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> joiner1 =
                Joiners.equal((a, b, c) -> a.add(b).add(c).longValue(), BigInteger::longValue);
        QuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> joiner2 =
                Joiners.lessThan((a, b, c) -> a.add(b).add(c).longValue(), BigInteger::longValue);
        AbstractQuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> mergedJoiner = merge(joiner1, joiner2);
        QuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> joiner3 =
                Joiners.greaterThan((a, b, c) -> a.add(b).add(c).longValue(), BigInteger::longValue);
        AbstractQuadJoiner<BigDecimal, BigDecimal, BigDecimal, BigInteger> reMergedJoiner = merge(mergedJoiner, joiner3);
        assertSoftly(softly -> {
            softly.assertThat(reMergedJoiner).isInstanceOf(CompositeQuadJoiner.class);
            softly.assertThat(reMergedJoiner.getJoinerTypes())
                    .containsExactly(JoinerType.EQUAL, JoinerType.LESS_THAN, JoinerType.GREATER_THAN);
            softly.assertThatThrownBy(() -> reMergedJoiner.getLeftMapping(3)).isInstanceOf(IllegalArgumentException.class);
            softly.assertThatThrownBy(() -> reMergedJoiner.getRightMapping(3)).isInstanceOf(IllegalArgumentException.class);
        });
    }

}
