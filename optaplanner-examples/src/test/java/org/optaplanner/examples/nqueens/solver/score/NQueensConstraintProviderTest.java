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
import org.optaplanner.examples.nqueens.domain.Column;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;
import org.optaplanner.examples.nqueens.domain.Row;
import org.optaplanner.test.impl.score.stream.ConstraintVerifier;
import org.optaplanner.test.impl.score.stream.SingleConstraintVerifier;

public class NQueensConstraintProviderTest {

    private final NQueensConstraintProvider constraintProvider = new NQueensConstraintProvider();
    private final ConstraintVerifier<NQueens> constraintVerifier =
            ConstraintVerifier.createFor(NQueens.class, Queen.class);

    private final Row row = new Row(0);
    private final Column column1 = new Column(0);
    private final Column column2 = new Column(1);
    private final Column column3 = new Column(2);

    @Test
    public void noHorizontalConflictWithOneQueen() {
        Queen queen1 = new Queen(0, row, column1);
        SingleConstraintVerifier<NQueens> horizontalConflictConstraintVerifier =
                constraintVerifier.forConstraint(constraintProvider::horizontalConflict);
        horizontalConflictConstraintVerifier.given(queen1, row, column1)
                .expectNoImpact();
    }

    @Test
    public void horizontalConflictWithTwoQueens() {
        Queen queen1 = new Queen(0, row, column1);
        Queen queen2 = new Queen(1, row, column2);
        SingleConstraintVerifier<NQueens> horizontalConflictConstraintVerifier =
                constraintVerifier.forConstraint(constraintProvider::horizontalConflict);
        horizontalConflictConstraintVerifier.given(queen1, queen2, row, column1, column2)
                .expectReward(1, "One pair of queens on the same row.");
        // Three queens
        Queen queen3 = new Queen(2, row, column3);
        horizontalConflictConstraintVerifier.given(queen1, queen2, queen3, row, column1, column2, column3)
                .expectReward(3, "Three pairs of queens on the same row.");
        // Intentionally broken to see the broken expectation message.
        horizontalConflictConstraintVerifier.given(queen1, queen2, queen3, row, column1, column2, column3)
                .expectReward(1, "Three pairs of queens on the same row.");
    }

    @Test
    public void horizontalConflictWithThreeQueens() {
        Queen queen1 = new Queen(0, row, column1);
        Queen queen2 = new Queen(1, row, column2);
        Queen queen3 = new Queen(2, row, column3);
        SingleConstraintVerifier<NQueens> horizontalConflictConstraintVerifier =
                constraintVerifier.forConstraint(constraintProvider::horizontalConflict);
        horizontalConflictConstraintVerifier.given(queen1, queen2, queen3, row, column1, column2, column3)
                .expectReward(3);
    }

}
