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

package org.optaplanner.test.impl.score.stream;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.optaplanner.constraint.streams.common.AbstractConstraint;
import org.optaplanner.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.DefaultScoreExplanation;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.util.Pair;
import org.optaplanner.test.api.score.stream.SingleConstraintAssertion;

public final class DefaultSingleConstraintAssertion<Solution_, Score_ extends Score<Score_>>
        implements SingleConstraintAssertion {

    private final AbstractConstraint<Solution_, ?, ?> constraint;
    private final ScoreDefinition<Score_> scoreDefinition;
    private final Score_ score;
    private final Collection<ConstraintMatchTotal<Score_>> constraintMatchTotalCollection;
    private final Collection<Indictment<Score_>> indictmentCollection;

    DefaultSingleConstraintAssertion(AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> scoreDirectorFactory,
            Score_ score, Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap,
            Map<Object, Indictment<Score_>> indictmentMap) {
        this.constraint = (AbstractConstraint<Solution_, ?, ?>) scoreDirectorFactory.getConstraints()[0];
        this.scoreDefinition = scoreDirectorFactory.getScoreDefinition();
        this.score = requireNonNull(score);
        this.constraintMatchTotalCollection = new ArrayList<>(requireNonNull(constraintMatchTotalMap).values());
        this.indictmentCollection = new ArrayList<>(requireNonNull(indictmentMap).values());
    }

    @Override
    public void penalizesBy(int matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.PENALTY, matchWeightTotal, message);
    }

    @Override
    public void penalizesBy(long matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.PENALTY, matchWeightTotal, message);
    }

    @Override
    public void penalizesBy(BigDecimal matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.PENALTY, matchWeightTotal, message);
    }

    @Override
    public void penalizes(long times, String message) {
        assertMatchCount(ScoreImpactType.PENALTY, times, message);
    }

    @Override
    public void penalizes(String message) {
        assertMatch(ScoreImpactType.PENALTY, message);
    }

    @Override
    public void rewardsWith(int matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.REWARD, matchWeightTotal, message);
    }

    @Override
    public void rewardsWith(long matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.REWARD, matchWeightTotal, message);
    }

    @Override
    public void rewardsWith(BigDecimal matchWeightTotal, String message) {
        validateMatchWeighTotal(matchWeightTotal);
        assertImpact(ScoreImpactType.REWARD, matchWeightTotal, message);
    }

    @Override
    public void rewards(long times, String message) {
        assertMatchCount(ScoreImpactType.REWARD, times, message);
    }

    @Override
    public void rewards(String message) {
        assertMatch(ScoreImpactType.REWARD, message);
    }

    private void validateMatchWeighTotal(Number matchWeightTotal) {
        if (matchWeightTotal.doubleValue() < 0) {
            throw new IllegalArgumentException("The matchWeightTotal (" + matchWeightTotal + ") must be positive.");
        }
    }

    private void assertImpact(ScoreImpactType scoreImpactType, Number matchWeightTotal, String message) {
        BiPredicate<Number, Number> equalityPredicate =
                NumberEqualityUtil.getEqualityPredicate(scoreDefinition, matchWeightTotal);
        Pair<Number, Number> deducedImpacts = deduceImpact();
        Number impact = deducedImpacts.getKey();
        ScoreImpactType actualScoreImpactType = constraint.getScoreImpactType();
        if (actualScoreImpactType == ScoreImpactType.MIXED) {
            // Impact means we need to check for expected impact type and actual impact match.
            switch (scoreImpactType) {
                case REWARD:
                    Number negatedImpact = deducedImpacts.getValue();
                    if (equalityPredicate.test(matchWeightTotal, negatedImpact)) {
                        return;
                    }
                    break;
                case PENALTY:
                    if (equalityPredicate.test(matchWeightTotal, impact)) {
                        return;
                    }
                    break;
            }
        } else if (actualScoreImpactType == scoreImpactType && equalityPredicate.test(matchWeightTotal, impact)) {
            // Reward and positive or penalty and negative means all is OK.
            return;
        }
        String constraintId = constraint.getConstraintId();
        String assertionMessage = buildAssertionErrorMessage(scoreImpactType, matchWeightTotal, actualScoreImpactType,
                impact, constraintId, message);
        throw new AssertionError(assertionMessage);
    }

    /**
     * Returns sum total of constraint match impacts,
     * deduced from constraint matches.
     *
     * @return never null; key is the deduced impact, the value its negation
     */
    private Pair<Number, Number> deduceImpact() {
        Score_ zeroScore = scoreDefinition.getZeroScore();
        Number zero = zeroScore.toLevelNumbers()[0]; // Zero in the exact numeric type expected by the caller.
        if (constraintMatchTotalCollection.isEmpty()) {
            return Pair.of(zero, zero);
        }
        // We do not know the matchWeight, so we need to deduce it.
        // Constraint matches give us a score, whose levels are in the form of (matchWeight * constraintWeight).
        // Here, we strip the constraintWeight.
        Score_ totalMatchWeightedScore = constraintMatchTotalCollection.stream()
                .map(matchScore -> scoreDefinition.divideBySanitizedDivisor(matchScore.getScore(),
                        matchScore.getConstraintWeight()))
                .reduce(zeroScore, Score::add);
        // Each level of the resulting score now has to be the same number, the matchWeight.
        // Except for where the number is zero.
        Number deducedImpact = retrieveImpact(totalMatchWeightedScore, zero);
        if (deducedImpact.equals(zero)) {
            return Pair.of(zero, zero);
        }
        Number negatedDeducedImpact = retrieveImpact(totalMatchWeightedScore.negate(), zero);
        return Pair.of(deducedImpact, negatedDeducedImpact);
    }

    private Number retrieveImpact(Score_ score, Number zero) {
        Number[] levelNumbers = score.toLevelNumbers();
        List<Number> impacts = Arrays.stream(levelNumbers)
                .distinct()
                .filter(matchWeight -> !Objects.equals(matchWeight, zero))
                .collect(Collectors.toList());
        switch (impacts.size()) {
            case 0:
                return zero;
            case 1:
                return impacts.get(0);
            default:
                throw new IllegalStateException("Impossible state: expecting at most one match weight (" +
                        impacts.size() + ") in matchWeightedScore level numbers (" + Arrays.toString(levelNumbers) + ").");
        }
    }

    private void assertMatchCount(ScoreImpactType scoreImpactType, long expectedMatchCount, String message) {
        long actualMatchCount = determineMatchCount(scoreImpactType);
        if (actualMatchCount == expectedMatchCount) {
            return;
        }
        String constraintId = constraint.getConstraintId();
        String assertionMessage =
                buildAssertionErrorMessage(scoreImpactType, expectedMatchCount, actualMatchCount, constraintId, message);
        throw new AssertionError(assertionMessage);
    }

    private void assertMatch(ScoreImpactType scoreImpactType, String message) {
        if (determineMatchCount(scoreImpactType) > 0) {
            return;
        }
        String constraintId = constraint.getConstraintId();
        String assertionMessage = buildAssertionErrorMessage(scoreImpactType, constraintId, message);
        throw new AssertionError(assertionMessage);
    }

    private long determineMatchCount(ScoreImpactType scoreImpactType) {
        if (constraintMatchTotalCollection.isEmpty()) {
            return 0;
        }
        ScoreImpactType actualImpactType = constraint.getScoreImpactType();

        if (actualImpactType != scoreImpactType && actualImpactType != ScoreImpactType.MIXED) {
            return 0;
        }
        Score_ zeroScore = scoreDefinition.getZeroScore();
        return constraintMatchTotalCollection.stream()
                .mapToLong(constraintMatchTotal -> {
                    if (actualImpactType == ScoreImpactType.MIXED) {
                        boolean isImpactPositive = constraintMatchTotal.getScore().compareTo(zeroScore) > 0;
                        boolean isImpactNegative = constraintMatchTotal.getScore().compareTo(zeroScore) < 0;
                        if (isImpactPositive && scoreImpactType == ScoreImpactType.PENALTY) {
                            return constraintMatchTotal.getConstraintMatchSet().size();
                        } else if (isImpactNegative && scoreImpactType == ScoreImpactType.REWARD) {
                            return constraintMatchTotal.getConstraintMatchSet().size();
                        } else {
                            return 0;
                        }
                    } else {
                        return constraintMatchTotal.getConstraintMatchSet().size();
                    }
                })
                .sum();
    }

    private String buildAssertionErrorMessage(ScoreImpactType expectedImpactType, Number expectedImpact,
            ScoreImpactType actualImpactType, Number actualImpact, String constraintId, String message) {
        String expectation = message != null ? message : "Broken expectation.";
        String preformattedMessage = "%s%n" +
                "%18s: %s%n" +
                "%18s: %s (%s)%n" +
                "%18s: %s (%s)%n%n" +
                "  %s";
        String expectedImpactLabel = "Expected " + getImpactTypeLabel(expectedImpactType);
        String actualImpactLabel = "Actual " + getImpactTypeLabel(actualImpactType);
        return String.format(preformattedMessage,
                expectation,
                "Constraint", constraintId,
                expectedImpactLabel, expectedImpact, expectedImpact.getClass(),
                actualImpactLabel, actualImpact, actualImpact.getClass(),
                DefaultScoreExplanation.explainScore(score, constraintMatchTotalCollection, indictmentCollection));
    }

    private String buildAssertionErrorMessage(ScoreImpactType impactType, long expectedTimes, long actualTimes,
            String constraintId, String message) {
        String expectation = message != null ? message : "Broken expectation.";
        String preformattedMessage = "%s%n" +
                "%18s: %s%n" +
                "%18s: %s time(s)%n" +
                "%18s: %s time(s)%n%n" +
                "  %s";
        String expectedImpactLabel = "Expected " + getImpactTypeLabel(impactType);
        String actualImpactLabel = "Actual " + getImpactTypeLabel(impactType);
        return String.format(preformattedMessage,
                expectation,
                "Constraint", constraintId,
                expectedImpactLabel, expectedTimes,
                actualImpactLabel, actualTimes,
                DefaultScoreExplanation.explainScore(score, constraintMatchTotalCollection, indictmentCollection));
    }

    private String buildAssertionErrorMessage(ScoreImpactType impactType, String constraintId, String message) {
        String expectation = message != null ? message : "Broken expectation.";
        String preformattedMessage = "%s%n" +
                "%18s: %s%n" +
                "%18s but there was none.%n%n" +
                "  %s";
        String expectedImpactLabel = "Expected " + getImpactTypeLabel(impactType);
        return String.format(preformattedMessage,
                expectation,
                "Constraint", constraintId,
                expectedImpactLabel,
                DefaultScoreExplanation.explainScore(score, constraintMatchTotalCollection, indictmentCollection));
    }

    private String getImpactTypeLabel(ScoreImpactType scoreImpactType) {
        if (scoreImpactType == ScoreImpactType.PENALTY) {
            return "penalty";
        } else if (scoreImpactType == ScoreImpactType.REWARD) {
            return "reward";
        } else { // Needs to work with null.
            return "impact";
        }
    }

}
