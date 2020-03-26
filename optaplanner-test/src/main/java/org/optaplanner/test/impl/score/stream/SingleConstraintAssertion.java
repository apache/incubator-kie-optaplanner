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

package org.optaplanner.test.impl.score.stream;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.Constraint;

public final class SingleConstraintAssertion<Solution_> extends AbstractAssertion<Solution_,
        SingleConstraintAssertion<Solution_>, SingleConstraintVerifier<Solution_>> {

    private final Map<String, ConstraintMatchTotal> constraintMatchTotalMap;

    SingleConstraintAssertion(SingleConstraintVerifier<Solution_> singleConstraintVerifier,
            Map<String, ConstraintMatchTotal> constraintMatchTotalMap) {
        super(singleConstraintVerifier);
        this.constraintMatchTotalMap = Collections.unmodifiableMap(constraintMatchTotalMap);
    }

    private Number getImpact() {
        return constraintMatchTotalMap.values().stream()
                .mapToInt(ConstraintMatchTotal::getConstraintMatchCount)
                .sum();
    }

    private static void assertCorrectMatchWeight(Number matchWeightTotal) {
        if (matchWeightTotal.doubleValue() <= 0) {
            throw new IllegalArgumentException("Expected a positive match weight, given (" + matchWeightTotal + ").");
        }
    }

    private void assertImpact(Number weight, String message) {
        Number impact = getImpact();
        String constraintId = getParentConstraintVerifier().getConstraint().getConstraintId();
        if (weight.equals(impact)) {
            return;
        }
        if (message == null) {
            throw new AssertionError("Broken expectation." + System.lineSeparator() +
                    "         Constraint: " + constraintId + System.lineSeparator() +
                    "    Expected impact: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                    "      Actual impact: " + impact + " (" + impact.getClass() + ")");
        }
        throw new AssertionError("Broken expectation. " + System.lineSeparator() +
                "            Message: " + message + System.lineSeparator() +
                "         Constraint: " + constraintId + System.lineSeparator() +
                "    Expected impact: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                "      Actual impact: " + impact + " (" + impact.getClass() + ")");
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in a specific penalty.
     *
     * @param matchWeightTotal sum of weights of constraint matches from applying the given facts to the constraint
     * @param message optional description of the scenario being asserted
     * @throws AssertionError when the expected penalty is not observed
     */
    public void expectPenalty(int matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(-matchWeightTotal, message);
    }

    /**
     * As defined by {@link #expectPenalty(int, String)}.
     */
    public void expectPenalty(long matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(-matchWeightTotal, message);
    }

    /**
     * As defined by {@link #expectPenalty(int, String)}.
     */
    public void expectPenalty(BigDecimal matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(matchWeightTotal.negate(), message);
    }

    /**
     * As defined by {@link #expectPenalty(int, String)} with a null message.
     */
    public void expectPenalty(int matchWeightTotal) {
        expectPenalty(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #expectPenalty(int, String)} with a null message.
     */
    public void expectPenalty(long matchWeightTotal) {
        expectPenalty(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #expectPenalty(int, String)} with a null message.
     */
    public void expectPenalty(BigDecimal matchWeightTotal) {
        expectPenalty(matchWeightTotal, null);
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in a specific reward.
     *
     * @param matchWeightTotal sum of weights of constraint matches from applying the given facts to the constraint
     * @param message optional description of the scenario being asserted
     * @throws AssertionError when the expected reward is not observed
     */
    public void expectReward(int matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    /**
     * As defined by {@link #expectReward(int, String)}.
     */
    public void expectReward(long matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    /**
     * As defined by {@link #expectReward(int, String)}.
     */
    public void expectReward(BigDecimal matchWeightTotal, String message) {
        assertCorrectMatchWeight(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    /**
     * As defined by {@link #expectReward(int, String)} with a null message.
     */
    public void expectReward(int matchWeightTotal) {
        expectReward(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #expectReward(int, String)} with a null message.
     */
    public void expectReward(long matchWeightTotal) {
        expectReward(matchWeightTotal, null);
    }

    /**
     * As defined by {@link #expectReward(int, String)} with a null message.
     */
    public void expectReward(BigDecimal matchWeightTotal) {
        expectReward(matchWeightTotal, null);
    }

    /**
     * Asserts that the {@link Constraint} under test, given a set of facts, results in neither penalty nor reward.
     *
     * @param message optional description of the scenario being asserted
     * @throws AssertionError when either a penalty or a reward is observed
     */
    public void expectNoImpact(String message) {
        assertImpact(0, message);
    }

    /**
     * As defined by {@link #expectNoImpact(String)} with a null message.
     */
    public void expectNoImpact() {
        expectNoImpact(null);
    }

}
