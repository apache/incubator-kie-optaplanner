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
package org.optaplanner.core.impl.score.stream.common;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;

public class IntervalJoinersTest {
    private static final class Interval<A, B> {
        final A start;
        final A end;
        final Function<A, B> map;

        public Interval(A start, A end, Function<A, B> map) {
            this.start = start;
            this.end = end;
            this.map = map;
        }

        public B getStart() {
            return map.apply(start);
        }

        public B getEnd() {
            return map.apply(end);
        }

        @Override
        public String toString() {
            return "(" + start.toString() + ", " + end.toString() + ")";
        }

        public static Interval<Long, BigDecimal> ofBigDecimal(Long start, Long end) {
            return new Interval<>(start, end, BigDecimal::valueOf);
        }

        public static Interval<Long, BigInteger> ofBigInt(Long start, Long end) {
            return new Interval<>(start, end, BigInteger::valueOf);
        }
    }

    @Test
    public void overlapsBi() {
        Function<Interval<Long, BigInteger>, Long> leftStartMapping = interval -> interval.getStart().longValue();
        Function<Interval<Long, BigInteger>, Long> leftEndMapping = interval -> interval.getEnd().longValue();
        Function<Interval<Long, BigDecimal>, Long> rightStartMapping = interval -> interval.getStart().longValue();
        Function<Interval<Long, BigDecimal>, Long> rightEndMapping = interval -> interval.getEnd().longValue();

        AbstractBiJoiner<Interval<Long, BigInteger>, Interval<Long, BigDecimal>> joiner =
                (AbstractBiJoiner<Interval<Long, BigInteger>, Interval<Long, BigDecimal>>) Joiners.overlaps(leftStartMapping,
                        leftEndMapping, rightStartMapping, rightEndMapping);

        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 5L),
                    Interval.ofBigDecimal(1L, 5L)))
                    .as("Case A = B").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 7L),
                    Interval.ofBigDecimal(1L, 5L)))
                    .as("B starts before A, A ends after B").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 5L),
                    Interval.ofBigDecimal(3L, 7L)))
                    .as("A starts before B, B ends after A").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 5L),
                    Interval.ofBigDecimal(1L, 7L)))
                    .as("B contains A").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 7L),
                    Interval.ofBigDecimal(3L, 5L)))
                    .as("A contains B").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 3L),
                    Interval.ofBigDecimal(5L, 7L)))
                    .as("A before B").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(5L, 7L),
                    Interval.ofBigDecimal(1L, 3L)))
                    .as("B before A").isFalse();

            // This is false since typically, when overlaps is used,
            // end is exclusive, and start is inclusive,
            // so 0-5, 5-10 do not overlap
            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 3L),
                    Interval.ofBigDecimal(3L, 7L)))
                    .as("A meets B").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 7L),
                    Interval.ofBigDecimal(1L, 3L)))
                    .as("B meets A").isFalse();
        });
    }

    @Test
    public void duringBi() {
        Function<Interval<Long, BigInteger>, Long> leftStartMapping = interval -> interval.getStart().longValue();
        Function<Interval<Long, BigInteger>, Long> leftEndMapping = interval -> interval.getEnd().longValue();
        Function<Interval<Long, BigDecimal>, Long> rightStartMapping = interval -> interval.getStart().longValue();
        Function<Interval<Long, BigDecimal>, Long> rightEndMapping = interval -> interval.getEnd().longValue();

        AbstractBiJoiner<Interval<Long, BigInteger>, Interval<Long, BigDecimal>> joiner =
                (AbstractBiJoiner<Interval<Long, BigInteger>, Interval<Long, BigDecimal>>) Joiners.during(leftStartMapping,
                        leftEndMapping, rightStartMapping, rightEndMapping);

        assertSoftly(softly -> {
            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 5L),
                    Interval.ofBigDecimal(1L, 5L)))
                    .as("Case A = B").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 7L),
                    Interval.ofBigDecimal(3L, 5L)))
                    .as("A contains B").isTrue();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 3L),
                    Interval.ofBigDecimal(5L, 7L)))
                    .as("A before B").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(5L, 7L),
                    Interval.ofBigDecimal(1L, 3L)))
                    .as("B before A").isFalse();

            // This is false since typically, when overlaps is used,
            // end is exclusive, and start is inclusive,
            // so 0-5, 5-10 do not overlap
            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 3L),
                    Interval.ofBigDecimal(3L, 7L)))
                    .as("A meets B").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 7L),
                    Interval.ofBigDecimal(1L, 3L)))
                    .as("B meets A").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 5L),
                    Interval.ofBigDecimal(1L, 7L)))
                    .as("B contains A").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(3L, 7L),
                    Interval.ofBigDecimal(1L, 5L)))
                    .as("B starts before A, A ends after B").isFalse();

            softly.assertThat(joiner.matches(Interval.ofBigInt(1L, 5L),
                    Interval.ofBigDecimal(3L, 7L)))
                    .as("A starts before B, B ends after A").isFalse();
        });
    }
}
