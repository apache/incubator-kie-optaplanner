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

package org.optaplanner.core.impl.score.definition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;

/**
 * Abstract superclass for {@link ScoreDefinition}.
 * @see ScoreDefinition
 * @see HardSoftScoreDefinition
 */
public abstract class AbstractScoreDefinition<S extends Score<S>> implements ScoreDefinition<S>, Serializable {

    private final String[] levelLabels;

    protected static int divideScoreLevel(int score, int scoreDivisor) {
        return divideScoreLevel(score, scoreDivisor, null);
    }

    protected static int divideScoreLevel(int score, int scoreDivisor, String maybeLabel) {
        String label = maybeLabel == null ? "" : maybeLabel;
        if (scoreDivisor == 0) {
            if (score == 0) {
                return 0;
            } else {
                throw new ArithmeticException("Can not divide " + score + label + " by " + scoreDivisor + label + ".");
            }
        } else {
            return (int) Math.floor(score / (double) scoreDivisor);
        }
    }

    protected static long divideScoreLevel(long score, long scoreDivisor) {
        return divideScoreLevel(score, scoreDivisor, null);
    }

    protected static long divideScoreLevel(long score, long scoreDivisor, String maybeLabel) {
        String label = maybeLabel == null ? "" : maybeLabel;
        if (scoreDivisor == 0L) {
            if (score == 0L) {
                return 0L;
            } else {
                throw new ArithmeticException("Can not divide " + score + label + " by " + scoreDivisor + label + ".");
            }
        } else {
            return (long) Math.floor(score / (double) scoreDivisor);
        }
    }

    protected static double divideScoreLevel(double score, double scoreDivisor) {
        return divideScoreLevel(score, scoreDivisor, null);
    }

    protected static double divideScoreLevel(double score, double scoreDivisor, String maybeLabel) {
        String label = maybeLabel == null ? "" : maybeLabel;
        if (scoreDivisor == 0d) {
            if (score == 0d) {
                return 0d;
            } else {
                throw new ArithmeticException("Can not divide " + score + label + " by " + scoreDivisor + label + ".");
            }
        } else {
            return Math.floor(score / scoreDivisor);
        }
    }

    protected static BigDecimal divideScoreLevel(BigDecimal score, BigDecimal scoreDivisor) {
        return divideScoreLevel(score, scoreDivisor, null);
    }

    protected static BigDecimal divideScoreLevel(BigDecimal score, BigDecimal scoreDivisor, String maybeLabel) {
        String label = maybeLabel == null ? "" : maybeLabel;
        if (scoreDivisor.signum() == 0) {
            if (score.signum() == 0) {
                return BigDecimal.ZERO;
            } else {
                throw new ArithmeticException("Can not divide " + score + label + " by " + scoreDivisor + label + ".");
            }
        } else {
            return score.divide(scoreDivisor, score.scale(), RoundingMode.FLOOR);
        }
    }

    protected static int divideInitScore(int initScore, double divisor) {
        return (int) divideScoreLevel(initScore, divisor, "init");
    }

    /**
     * @param levelLabels never null, as defined by {@link ScoreDefinition#getLevelLabels()}
     */
    public AbstractScoreDefinition(String[] levelLabels) {
        this.levelLabels = levelLabels;
    }

    @Override
    public String getInitLabel() {
        return "init score";
    }

    @Override
    public int getLevelsSize() {
        return levelLabels.length;
    }

    @Override
    public String[] getLevelLabels() {
        return levelLabels;
    }

    @Override
    public String formatScore(S score) {
        return score.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
