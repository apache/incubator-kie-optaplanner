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

package org.optaplanner.core.api.score.buildin.simplelong;

import java.util.Objects;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.ScoreUtil;

/**
 * This {@link Score} is based on 1 level of long constraints.
 * <p>
 * This class is immutable.
 *
 * @see Score
 */
public final class SimpleLongScore implements Score<SimpleLongScore> {

    public static final SimpleLongScore ZERO = new SimpleLongScore(0, 0L);
    public static final SimpleLongScore ONE = new SimpleLongScore(0, 1L);

    public static SimpleLongScore parseScore(String scoreString) {
        String[] scoreTokens = ScoreUtil.parseScoreTokens(SimpleLongScore.class, scoreString, "");
        int initScore = ScoreUtil.parseInitScore(SimpleLongScore.class, scoreString, scoreTokens[0]);
        long score = ScoreUtil.parseLevelAsLong(SimpleLongScore.class, scoreString, scoreTokens[1]);
        return ofUninitialized(initScore, score);
    }

    public static SimpleLongScore ofUninitialized(int initScore, long score) {
        return new SimpleLongScore(initScore, score);
    }

    public static SimpleLongScore of(long score) {
        return new SimpleLongScore(0, score);
    }

    // ************************************************************************
    // Fields
    // ************************************************************************

    private final int initScore;
    private final long score;

    /**
     * Private default constructor for default marshalling/unmarshalling of unknown frameworks that use reflection.
     * Such integration is always inferior to the specialized integration modules, such as
     * optaplanner-persistence-jpa, optaplanner-persistence-jackson, optaplanner-persistence-jaxb, ...
     */
    @SuppressWarnings("unused")
    private SimpleLongScore() {
        this(Integer.MIN_VALUE, Long.MIN_VALUE);
    }

    private SimpleLongScore(int initScore, long score) {
        this.initScore = initScore;
        this.score = score;
    }

    @Override
    public int initScore() {
        return initScore;
    }

    /**
     * The total of the broken negative constraints and fulfilled positive constraints.
     * Their weight is included in the total.
     * The score is usually a negative number because most use cases only have negative constraints.
     *
     * @return higher is better, usually negative, 0 if no constraints are broken/fulfilled
     */
    public long score() {
        return score;
    }

    /**
     * As defined by {@link #score()}.
     *
     * @deprecated Use {@link #score()} instead.
     */
    @Deprecated(forRemoval = true)
    public long getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public SimpleLongScore withInitScore(int newInitScore) {
        return new SimpleLongScore(newInitScore, score);
    }

    @Override
    public SimpleLongScore add(SimpleLongScore addend) {
        return new SimpleLongScore(
                initScore + addend.initScore(),
                score + addend.score());
    }

    @Override
    public SimpleLongScore subtract(SimpleLongScore subtrahend) {
        return new SimpleLongScore(
                initScore - subtrahend.initScore(),
                score - subtrahend.score());
    }

    @Override
    public SimpleLongScore multiply(double multiplicand) {
        return new SimpleLongScore(
                (int) Math.floor(initScore * multiplicand),
                (long) Math.floor(score * multiplicand));
    }

    @Override
    public SimpleLongScore divide(double divisor) {
        return new SimpleLongScore(
                (int) Math.floor(initScore / divisor),
                (long) Math.floor(score / divisor));
    }

    @Override
    public SimpleLongScore power(double exponent) {
        return new SimpleLongScore(
                (int) Math.floor(Math.pow(initScore, exponent)),
                (long) Math.floor(Math.pow(score, exponent)));
    }

    @Override
    public SimpleLongScore abs() {
        return new SimpleLongScore(Math.abs(initScore), Math.abs(score));
    }

    @Override
    public SimpleLongScore zero() {
        return SimpleLongScore.ZERO;
    }

    @Override
    public boolean isFeasible() {
        return initScore >= 0;
    }

    @Override
    public Number[] toLevelNumbers() {
        return new Number[] { score };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SimpleLongScore) {
            SimpleLongScore other = (SimpleLongScore) o;
            return initScore == other.initScore()
                    && score == other.score();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(initScore, score);
    }

    @Override
    public int compareTo(SimpleLongScore other) {
        if (initScore != other.initScore()) {
            return Integer.compare(initScore, other.initScore());
        } else {
            return Long.compare(score, other.score());
        }
    }

    @Override
    public String toShortString() {
        return ScoreUtil.buildShortString(this, n -> n.longValue() != 0L, "");
    }

    @Override
    public String toString() {
        return ScoreUtil.getInitPrefix(initScore) + score;
    }

}
