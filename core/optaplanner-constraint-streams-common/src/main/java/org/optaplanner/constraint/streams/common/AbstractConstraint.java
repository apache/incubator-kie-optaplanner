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

package org.optaplanner.constraint.streams.common;

import java.math.BigDecimal;
import java.util.function.Function;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.Constraint;

public abstract class AbstractConstraint<Solution_, Constraint_ extends AbstractConstraint<Solution_, Constraint_, ConstraintFactory_>, ConstraintFactory_ extends InnerConstraintFactory<Solution_, Constraint_>>
        implements Constraint {

    private final ConstraintFactory_ constraintFactory;
    private final String constraintPackage;
    private final String constraintName;
    private final String constraintId;
    private final Function<Solution_, Score<?>> constraintWeightExtractor;
    private final ScoreImpactType scoreImpactType;
    private final boolean isConstraintWeightConfigurable;
    // Constraint is not generic in uni/bi/..., therefore these can not be typed.
    private final Object justificationMapping;
    private final Object indictedObjectsMapping;

    protected AbstractConstraint(ConstraintFactory_ constraintFactory, String constraintPackage, String constraintName,
            Function<Solution_, Score<?>> constraintWeightExtractor, ScoreImpactType scoreImpactType,
            boolean isConstraintWeightConfigurable, Object justificationMapping, Object indictedObjectsMapping) {
        this.constraintFactory = constraintFactory;
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        this.constraintWeightExtractor = constraintWeightExtractor;
        this.scoreImpactType = scoreImpactType;
        this.isConstraintWeightConfigurable = isConstraintWeightConfigurable;
        this.justificationMapping = justificationMapping;
        this.indictedObjectsMapping = indictedObjectsMapping;
    }

    public final <Score_ extends Score<Score_>> Score_ extractConstraintWeight(Solution_ workingSolution) {
        if (isConstraintWeightConfigurable && workingSolution == null) {
            /*
             * In constraint verifier API, we allow for testing constraint providers without having a planning solution.
             * However, constraint weights may be configurable and in that case the solution is required to read the
             * weights from.
             * For these cases, we set the constraint weight to the softest possible value, just to make sure that the
             * constraint is not ignored.
             * The actual value is not used in any way.
             */
            return (Score_) constraintFactory.getSolutionDescriptor().getScoreDefinition().getOneSoftestScore();
        }
        Score_ constraintWeight = (Score_) constraintWeightExtractor.apply(workingSolution);
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

    public final void assertCorrectImpact(int impact) {
        if (scoreImpactType == ScoreImpactType.MIXED) { // No need to do anything.
            return;
        }
        if (impact < 0) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    public final void assertCorrectImpact(long impact) {
        if (scoreImpactType == ScoreImpactType.MIXED) { // No need to do anything.
            return;
        }
        if (impact < 0L) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    public final void assertCorrectImpact(BigDecimal impact) {
        if (scoreImpactType == ScoreImpactType.MIXED) { // No need to do anything.
            return;
        }
        if (impact.signum() < 0) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    @Override
    public final ConstraintFactory_ getConstraintFactory() {
        return constraintFactory;
    }

    @Override
    public final String getConstraintPackage() {
        return constraintPackage;
    }

    @Override
    public final String getConstraintName() {
        return constraintName;
    }

    @Override
    public final String getConstraintId() { // Overridden in order to cache the string concatenation.
        return constraintId;
    }

    public final ScoreImpactType getScoreImpactType() {
        return scoreImpactType;
    }

    public <JustificationMapping_> JustificationMapping_ getJustificationMapping() {
        // It is the job of the code constructing the constraint to ensure that this cast is correct.
        return (JustificationMapping_) justificationMapping;
    }

    public <IndictedObjectsMapping_> IndictedObjectsMapping_ getIndictedObjectsMapping() {
        // It is the job of the code constructing the constraint to ensure that this cast is correct.
        return (IndictedObjectsMapping_) indictedObjectsMapping;
    }

}
