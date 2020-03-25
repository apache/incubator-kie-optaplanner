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

public abstract class AbstractConstraintVerifierAssertion<A extends AbstractConstraintVerifierAssertion<A, V>,
        V extends AbstractConstraintVerifier<A, V>> {

    private final V parentConstraintVerifier;

    protected AbstractConstraintVerifierAssertion(V constraintVerifier) {
        this.parentConstraintVerifier = constraintVerifier;
    }

    private static void assertPositive(Number matchWeightTotal) {
        if (matchWeightTotal.doubleValue() <= 0) {
            throw new IllegalArgumentException("expectReward() requires a positive match weight, given (" + matchWeightTotal + ")");
        }
    }

    private static void assertNegative(Number matchWeightTotal) {
        if (matchWeightTotal.doubleValue() >= 0) {
            throw new IllegalArgumentException("expectPenalty() requires a negative match weight, given (" + matchWeightTotal + ")");
        }
    }

    protected final V getParentConstraintVerifier() {
        return parentConstraintVerifier;
    }

    abstract protected Number getImpact();

    private void assertImpact(Number weight, String message) {
        Number impact = getImpact();
        String constraintId = getParentConstraintVerifier().getConstraint().getConstraintId();
        if (weight.equals(impact)) {
            return;
        }
        if (message == null) {
            throw new AssertionError("Broken expectation." + System.lineSeparator() +
                    "  Constraint: " + constraintId + System.lineSeparator() +
                    "    Expected: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                    "      Actual: " + impact + " (" + impact.getClass() + ")");
        }
        throw new AssertionError("Broken expectation. " + System.lineSeparator() +
                "     Message: " + message + System.lineSeparator() +
                "  Constraint: " + constraintId + System.lineSeparator() +
                "    Expected: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                "      Actual: " + impact + " (" + impact.getClass() + ")");
    }

    public void expectPenalty(String message, int matchWeightTotal) {
        assertNegative(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectPenalty(String message, long matchWeightTotal) {
        assertNegative(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectPenalty(String message, BigDecimal matchWeightTotal) {
        assertNegative(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectPenalty(int matchWeightTotal) {
        expectPenalty(null, matchWeightTotal);
    }

    public void expectPenalty(long matchWeightTotal) {
        expectPenalty(null, matchWeightTotal);
    }

    public void expectPenalty(BigDecimal matchWeightTotal) {
        expectPenalty(null, matchWeightTotal);
    }

    public void expectReward(String message, int matchWeightTotal) {
        assertPositive(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectReward(String message, long matchWeightTotal) {
        assertPositive(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectReward(String message, BigDecimal matchWeightTotal) {
        assertPositive(matchWeightTotal);
        assertImpact(matchWeightTotal, message);
    }

    public void expectReward(int matchWeightTotal) {
        expectReward(null, matchWeightTotal);
    }

    public void expectReward(long matchWeightTotal) {
        expectReward(null, matchWeightTotal);
    }

    public void expectReward(BigDecimal matchWeightTotal) {
        expectReward(null, matchWeightTotal);
    }

    public void expectNoImpact(String message) {
        assertImpact(0, message);
    }

    public void expectNoImpact() {
        expectNoImpact(null);
    }
}
