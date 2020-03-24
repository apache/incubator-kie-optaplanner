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

    protected final V getParentConstraintVerifier() {
        return parentConstraintVerifier;
    }

    abstract protected Number getImpact();

    private void assertImpact(Number weight, String message) {
        Number impact = getImpact();
        if (!weight.equals(impact)) {
            if (message == null) {
                throw new IllegalStateException("Broken expectation." + System.lineSeparator() +
                        "    Expected: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                        "      Actual: " + impact + " (" + impact.getClass() + ")");
            } else {
                throw new IllegalStateException("Broken expectation. " + System.lineSeparator() +
                        "     Message: " + message + System.lineSeparator() +
                        "    Expected: " + weight + " (" + weight.getClass() + ")" + System.lineSeparator() +
                        "      Actual: " + impact + " (" + impact.getClass() + ")");
            }
        }
    }

    public A expectImpact(String message, int matchWeight) {
        assertImpact(matchWeight, message);
        return (A) this;
    }

    public A expectImpact(String message, long matchWeight) {
        assertImpact(matchWeight, message);
        return (A) this;
    }

    public A expectImpact(String message, BigDecimal matchWeight) {
        assertImpact(matchWeight, message);
        return (A) this;
    }

    public A expectImpact(int matchWeight) {
        return expectImpact(null, matchWeight);
    }

    public A expectImpact(long matchWeight) {
        return expectImpact(null, matchWeight);
    }

    public A expectImpact(BigDecimal matchWeight) {
        return expectImpact(null, matchWeight);
    }

}
