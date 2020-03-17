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

import java.util.Arrays;
import java.util.Objects;

import org.optaplanner.core.api.score.Score;

public abstract class AbstractConstraintVerifierAssertion<A extends AbstractConstraintVerifierAssertion<A, V>,
        V extends AbstractConstraintVerifier<A, V>> {

    private final V parentConstraintVerifier;
    private final Object planningSolution;
    private final Object[] facts;

    protected AbstractConstraintVerifierAssertion(V constraintVerifier, Object planningSolution) {
        Objects.requireNonNull(planningSolution);
        this.parentConstraintVerifier = constraintVerifier;
        this.planningSolution = planningSolution;
        this.facts = null;
    }

    protected AbstractConstraintVerifierAssertion(V constraintVerifier, Object[] facts) {
        Objects.requireNonNull(facts);
        this.parentConstraintVerifier = constraintVerifier;
        this.planningSolution = null;
        this.facts = Arrays.copyOf(facts, facts.length);
    }

    protected final V getParentConstraintVerifier() {
        return parentConstraintVerifier;
    }

    public A expectImpact(Score<?> score) {
        return (A) this;
    }

}
