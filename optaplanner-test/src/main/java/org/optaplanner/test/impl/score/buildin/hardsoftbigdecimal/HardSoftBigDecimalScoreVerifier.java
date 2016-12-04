/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.test.impl.score.buildin.hardsoftbigdecimal;

import java.math.BigDecimal;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.test.impl.score.AbstractScoreVerifier;

/**
 * To assert the constraints (including score rules) of a {@link SolverFactory}
 * that uses a {@link HardSoftBigDecimalScore}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class HardSoftBigDecimalScoreVerifier<Solution_> extends AbstractScoreVerifier<Solution_> {

    /**
     * @param solverFactory never null, the {@link SolverFactory} of which you want to test the constraints.
     */
    public HardSoftBigDecimalScoreVerifier(SolverFactory<Solution_> solverFactory) {
        super(solverFactory, HardSoftBigDecimalScore.class);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight never null, the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertHardWeight(String constraintName, BigDecimal expectedWeight, Solution_ solution) {
        assertHardWeight(null, constraintName, expectedWeight, solution);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintPackage sometimes null.
     * When null, {@code constraintName} for the {@code scoreLevel} must be unique.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight never null, the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertHardWeight(String constraintPackage, String constraintName, BigDecimal expectedWeight, Solution_ solution) {
        assertWeight(constraintPackage, constraintName, 0, expectedWeight, solution);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight never null, the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertSoftWeight(String constraintName, BigDecimal expectedWeight, Solution_ solution) {
        assertSoftWeight(null, constraintName, expectedWeight, solution);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintPackage sometimes null.
     * When null, {@code constraintName} for the {@code scoreLevel} must be unique.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight never null, the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertSoftWeight(String constraintPackage, String constraintName, BigDecimal expectedWeight, Solution_ solution) {
        assertWeight(constraintPackage, constraintName, 1, expectedWeight, solution);
    }

}
