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

package org.optaplanner.core.api.score.stream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

import static java.math.BigInteger.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class JoinersTest {

    @Test
    public void equalBi() {
        Function<BigInteger, Long> leftMapping = BigInteger::longValue;
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractBiJoiner<BigInteger, BigDecimal> joiner =
                (AbstractBiJoiner<BigInteger, BigDecimal>) Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(TEN, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(ONE, BigDecimal.ZERO)).isFalse();
            softly.assertThatThrownBy(() -> joiner.getLeftMapping(1)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void equalTri() {
        BiFunction<BigInteger, BigInteger, Long> leftMapping = (a, b) -> a.add(b).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractTriJoiner<BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractTriJoiner<BigInteger, BigInteger, BigDecimal>) Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(TEN, ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(ONE, ZERO, BigDecimal.ZERO)).isFalse();
            softly.assertThatThrownBy(() -> joiner.getLeftMapping(1)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void equalQuad() {
        TriFunction<BigInteger, BigInteger, BigInteger, Long> leftMapping = (a, b, c) -> a.add(b).add(c).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractQuadJoiner<BigInteger, BigInteger, BigInteger, BigDecimal>)
                        Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(TEN, ZERO, ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(ONE, ZERO, ZERO, BigDecimal.ZERO)).isFalse();
            softly.assertThatThrownBy(() -> joiner.getLeftMapping(1)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void equalPenta() {
        QuadFunction<BigInteger, BigInteger, BigInteger, BigInteger, Long> leftMapping =
                (a, b, c, d) -> a.add(b).add(c).add(d).longValue();
        Function<BigDecimal, Long> rightMapping = BigDecimal::longValue;
        AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal> joiner =
                (AbstractPentaJoiner<BigInteger, BigInteger, BigInteger, BigInteger, BigDecimal>)
                        Joiners.equal(leftMapping, rightMapping);
        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(TEN, ZERO, ZERO, ZERO, BigDecimal.TEN)).isTrue();
            softly.assertThat(joiner.matches(ONE, ZERO, ZERO, ZERO, BigDecimal.ZERO)).isFalse();
            softly.assertThatThrownBy(() -> joiner.getLeftMapping(1)).isInstanceOf(IllegalArgumentException.class);
        });
    }

}
