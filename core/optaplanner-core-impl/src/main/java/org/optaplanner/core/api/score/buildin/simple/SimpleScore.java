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

package org.optaplanner.core.api.score.buildin.simple;

import java.util.Objects;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.ScoreUtil;

/**
 * This {@link Score} is based on 1 level of int constraints.
 * <p>
 * This class is immutable.
 *
 * @see Score
 */
public final class SimpleScore implements Score<SimpleScore> {

    public static final SimpleScore ZERO = new SimpleScore(0, 0);
    public static final SimpleScore ONE = new SimpleScore(0, 1);

    public static SimpleScore parseScore(String scoreString) {
        String[] scoreTokens = ScoreUtil.parseScoreTokens(SimpleScore.class, scoreString, "");
        int initScore = ScoreUtil.parseInitScore(SimpleScore.class, scoreString, scoreTokens[0]);
        int score = ScoreUtil.parseLevelAsInt(SimpleScore.class, scoreString, scoreTokens[1]);
        return ofUninitialized(initScore, score);
    }

    public static SimpleScore ofUninitialized(int initScore, int score) {
        return new SimpleScore(initScore, score);
    }

    public static SimpleScore of(int score) {
        return new SimpleScore(0, score);
    }

    // ************************************************************************
    // Fields
    // ************************************************************************

    private final int initScore;
    private final int score;

    /**
     * Private default constructor for default marshalling/unmarshalling of unknown frameworks that use reflection.
     * Such integration is always inferior to the specialized integration modules, such as
     * optaplanner-persistence-jpa, optaplanner-persistence-jackson, optaplanner-persistence-jaxb, ...
     */
    @SuppressWarnings("unused")
    private SimpleScore() {
        this(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    private SimpleScore(int initScore, int score) {
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
    public int score() {
        return score;
    }

    /**
     * As defined by {@link #score()}.
     *
     * @deprecated Use {@link #score()} instead.
     */
    @Deprecated(forRemoval = true)
    public int getScore() {
        return score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public SimpleScore withInitScore(int newInitScore) {
        return new SimpleScore(newInitScore, score);
    }

    @Override
    public SimpleScore add(SimpleScore addend) {
        return new SimpleScore(
                initScore + addend.initScore(),
                score + addend.score());
    }

    @Override
    public SimpleScore subtract(SimpleScore subtrahend) {
        return new SimpleScore(
                initScore - subtrahend.initScore(),
                score - subtrahend.score());
    }

    @Override
    public SimpleScore multiply(double multiplicand) {
        return new SimpleScore(
                (int) Math.floor(initScore * multiplicand),
                (int) Math.floor(score * multiplicand));
    }

    @Override
    public SimpleScore divide(double divisor) {
        return new SimpleScore(
                (int) Math.floor(initScore / divisor),
                (int) Math.floor(score / divisor));
    }

    @Override
    public SimpleScore power(double exponent) {
        return new SimpleScore(
                (int) Math.floor(Math.pow(initScore, exponent)),
                (int) Math.floor(Math.pow(score, exponent)));
    }

    @Override
    public SimpleScore abs() {
        return new SimpleScore(Math.abs(initScore), Math.abs(score));
    }

    @Override
    public SimpleScore zero() {
        return SimpleScore.ZERO;
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
        } else if (o instanceof SimpleScore) {
            SimpleScore other = (SimpleScore) o;
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
    public int compareTo(SimpleScore other) {
        if (initScore != other.initScore()) {
            return Integer.compare(initScore, other.initScore());
        } else {
            return Integer.compare(score, other.score());
        }
    }

    @Override
    public String toShortString() {
        return ScoreUtil.buildShortString(this, n -> n.intValue() != 0, "");
    }

    @Override
    public String toString() {
        return ScoreUtil.getInitPrefix(initScore) + score;
    }

}
