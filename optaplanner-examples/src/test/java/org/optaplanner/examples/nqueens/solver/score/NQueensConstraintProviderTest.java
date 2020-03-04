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
import org.optaplanner.test.impl.score.stream.ConstraintProviderVerifier;

import static org.optaplanner.test.impl.score.stream.ConstraintVerifier.forConstraint;
import static org.optaplanner.test.impl.score.stream.ConstraintVerifier.forConstraintProvider;

public class NQueensConstraintProviderTest {

    // Dummy data, the tests don't actually do anything.
    private Queen queen1, queen2;
    private Row row1, row2;

    @Test
    public void testSingleConstraint() {
        NQueensConstraintProvider constraintProvider = new NQueensConstraintProvider();
        forConstraint(constraintProvider::ascendingDiagonalConflict)
                .givenFacts(queen1, queen2, row1, row2)
                .expectImpact(SimpleScore.ONE)  // No type check here. Wrong score type fails at runtime.
                .expectConstraintMatches(queen1, queen2);
        //      ^^^^^^ We shouldn't allow this until we have a clear definition and correct implementation of
        //      constraint matches.
        //      I suggest that this is out of scope for now, and therefore so is this method.
    }

    @Test
    public void testEntireConstraintProvider() {
        forConstraintProvider(new NQueensConstraintProvider())
                .givenPlanningSolution(new NQueens()) // You would read some XStream solution here.
                .expectImpact(SimpleScore.ONE); // Does not allow to check constraint matches, pointless with Solution.
    }

    @Test
    public void testVerifierReuse() {
        ConstraintProviderVerifier constraintVerifier = forConstraintProvider(new NQueensConstraintProvider());
        constraintVerifier.givenFacts(queen1, queen2, row1, row2)
                .expectImpact(SimpleScore.ONE);
        constraintVerifier.givenFacts(queen2, row1)
                .expectImpact(SimpleScore.ZERO);
    }

}
