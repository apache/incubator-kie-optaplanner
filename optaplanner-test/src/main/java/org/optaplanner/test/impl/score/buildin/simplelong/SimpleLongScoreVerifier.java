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

package org.optaplanner.test.impl.score.buildin.simplelong;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.test.impl.score.AbstractScoreVerifier;

/**
 * To assert the constraints (including score rules) of a {@link SolverFactory}
 * that uses a {@link SimpleLongScore}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SimpleLongScoreVerifier<Solution_> extends AbstractScoreVerifier<Solution_> {

    /**
     * @param solverFactory never null, the {@link SolverFactory} of which you want to test the constraints.
     */
    public SimpleLongScoreVerifier(SolverFactory<Solution_> solverFactory) {
        super(solverFactory, SimpleLongScore.class);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertWeight(String constraintName, long expectedWeight, Solution_ solution) {
        assertWeight(null, constraintName, expectedWeight, solution);
    }

    /**
     * Assert that the constraint (which is usually a score rule) of {@link PlanningSolution}
     * has the expected weight for that score level.
     * @param constraintPackage sometimes null.
     * When null, {@code constraintName} for the {@code scoreLevel} must be unique.
     * @param constraintName never null, the name of the constraint, which is usually the name of the score rule
     * @param expectedWeight the total weight for all matches of that 1 constraint
     * @param solution never null, the actual {@link PlanningSolution}
     */
    public void assertWeight(String constraintPackage, String constraintName, long expectedWeight, Solution_ solution) {
        assertWeight(constraintPackage, constraintName, 0, Long.valueOf(expectedWeight), solution);
    }

}
