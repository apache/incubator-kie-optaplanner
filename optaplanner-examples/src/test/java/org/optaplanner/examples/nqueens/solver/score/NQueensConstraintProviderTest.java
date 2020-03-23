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

package org.optaplanner.examples.nqueens.solver.score;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;
import org.optaplanner.examples.nqueens.domain.Row;
import org.optaplanner.test.impl.score.stream.ConstraintVerifier;

public class NQueensConstraintProviderTest {

    // Dummy data, the tests don't actually do anything.
    private Queen queen1, queen2;
    private Row row1, row2;

    @Test
    public void testSingleConstraint() {
        NQueensConstraintProvider constraintProvider = new NQueensConstraintProvider();
        ConstraintVerifier.createFor(NQueens.class, Queen.class)
                .forConstraint(constraintProvider::ascendingDiagonalConflict)
                .givenFacts(queen1, queen2, row1, row2)
                .expectImpact(SimpleScore.ONE);  // No type check here. Wrong score type fails at runtime.
    }

}
