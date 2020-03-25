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

    @Test
    public void testSingleConstraint() {
        Column column1 = new Column();
        column1.setId(0L);
        column1.setIndex(0);
        Row row = new Row();
        row.setId(0L);
        row.setIndex(0);
        Queen queen1 = new Queen();
        queen1.setId(0L);
        queen1.setRow(row);
        queen1.setColumn(column1);
        // One queen
        SingleConstraintVerifier<NQueens> horizontalConflictConstraintVerifier =
                constraintVerifier.forConstraint(constraintProvider::horizontalConflict);
        horizontalConflictConstraintVerifier.given(queen1, row, column1)
                .expectNoImpact("No horizontal conflicts with just one queen.");
        // Two queens
        Column column2 = new Column();
        column2.setId(1L);
        column2.setIndex(1);
        Queen queen2 = new Queen();
        queen2.setId(1L);
        queen2.setRow(row);
        queen2.setColumn(column2);
        horizontalConflictConstraintVerifier.given(queen1, queen2, row, column1, column2)
                .expectReward("One pair of queens on the same row.", 1);
        // Three queens
        Column column3 = new Column();
        column2.setId(2L);
        column2.setIndex(2);
        Queen queen3 = new Queen();
        queen3.setId(2L);
        queen3.setRow(row);
        queen3.setColumn(column3);
        horizontalConflictConstraintVerifier.given(queen1, queen2, queen3, row, column1, column2, column3)
                .expectReward("Three pairs of queens on the same row.", 3);
        // Intentionally broken to see the broken expectation message.
        horizontalConflictConstraintVerifier.given(queen1, queen2, queen3, row, column1, column2, column3)
                .expectReward("Three pairs of queens on the same row.", 1);
    }

}
