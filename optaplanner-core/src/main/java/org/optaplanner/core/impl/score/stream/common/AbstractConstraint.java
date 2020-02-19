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

import java.math.BigDecimal;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ScoreImpactType;
import org.optaplanner.core.impl.score.stream.InnerConstraintFactory;

public abstract class AbstractConstraint<Solution_, ConstraintFactory extends InnerConstraintFactory<Solution_>>
        implements Constraint {

    protected final ConstraintFactory constraintFactory;
    protected final String constraintPackage;
    protected final String constraintName;
    private final Function<Solution_, Score<?>> constraintWeightExtractor;
    protected final ScoreImpactType scoreImpactType;

    protected AbstractConstraint(ConstraintFactory constraintFactory, String constraintPackage, String constraintName,
            Function<Solution_, Score<?>> constraintWeightExtractor, ScoreImpactType scoreImpactType) {
        this.constraintFactory = constraintFactory;
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.constraintWeightExtractor = constraintWeightExtractor;
        this.scoreImpactType = scoreImpactType;
    }

    public Score<?> extractConstraintWeight(Solution_ workingSolution) {
        Score<?> constraintWeight = constraintWeightExtractor.apply(workingSolution);
        constraintFactory.getSolutionDescriptor().validateConstraintWeight(constraintPackage, constraintName, constraintWeight);
        switch (scoreImpactType) {
            case PENALTY:
                return constraintWeight.negate();
            case REWARD:
            case MIXED:
                return constraintWeight;
            default:
                throw new IllegalStateException("Unknown score impact type: (" + scoreImpactType + ")");
        }
    }

    public void assertCorrectImpact(int impact) {
        assertCorrectImpact(impact, () -> impact < 0, () -> impact > 0);
    }

    public void assertCorrectImpact(long impact) {
        assertCorrectImpact(impact, () -> impact < 0L, () -> impact > 0L);
    }

    public void assertCorrectImpact(BigDecimal impact) {
        assertCorrectImpact(impact, () -> impact.signum() < 0, () -> impact.compareTo(BigDecimal.ZERO) > 0);
    }

    private void throwOnNegativeImpact(Object impact) {
        throw new IllegalStateException("Negative match weight (" + impact + ") for constraint (" + getConstraintId() + ") reward. " +
                "Check constraint provider implementation.");
    }

    private void throwOnPositiveImpact(Object impact) {
        throw new IllegalStateException("Positive match weight (" + impact + ") for constraint (" + getConstraintId() + ") penalty. " +
                "Check constraint provider implementation.");
    }

    private void assertCorrectImpact(Object impact, BooleanSupplier lessThanZero, BooleanSupplier moreThanZero) {
        switch (scoreImpactType) {
            case MIXED: // No need to do anything.
                break;
            case REWARD:
                if (lessThanZero.getAsBoolean()) {
                    throwOnNegativeImpact(impact);
                }
                return;
            case PENALTY:
                if (moreThanZero.getAsBoolean()) {
                    throwOnPositiveImpact(impact);
                }
                return;
            default:
                throw new IllegalStateException("Unknown score impact type: (" + scoreImpactType + ")");
        }
    }

    @Override
    public ConstraintFactory getConstraintFactory() {
        return constraintFactory;
    }

    @Override
    public String getConstraintPackage() {
        return constraintPackage;
    }

    @Override
    public String getConstraintName() {
        return constraintName;
    }

    @Override
    public ScoreImpactType getScoreImpactType() {
        return scoreImpactType;
    }

}
