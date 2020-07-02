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

package org.optaplanner.core.impl.score.stream.drools;

import java.util.function.Function;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.stream.common.AbstractConstraint;
import org.optaplanner.core.impl.score.stream.common.ScoreImpactType;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsConstraint<Solution_> extends AbstractConstraint<Solution_, DroolsConstraintFactory<Solution_>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsConstraint.class);
    private final ConstraintConsequence consequence;

    public DroolsConstraint(DroolsConstraintFactory<Solution_> constraintFactory, String constraintPackage,
            String constraintName, Function<Solution_, Score<?>> constraintWeightExtractor,
            ScoreImpactType scoreImpactType, boolean isConstraintWeightConfigurable,
            ConstraintConsequence constraintConsequence) {
        super(constraintFactory, constraintPackage, constraintName, constraintWeightExtractor, scoreImpactType,
                isConstraintWeightConfigurable);
        this.consequence = constraintConsequence;
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    /**
     * As defined by {@link DroolsRuleStructure#getExpectedJustificationTypes()}.
     *
     * @return never null, never empty
     */
    public Class[] getExpectedJustificationTypes() {
        return null;
    }

    public int getExpectedJustificationCount() {
        return consequence.getTerminalNode().getCardinality();
    }

    @Override
    public String toString() {
        return "DroolsConstraint(" + getConstraintId() + ")";
    }
}
