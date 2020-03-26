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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.examples.nqueens.domain.Column;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;
import org.optaplanner.examples.nqueens.domain.Row;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.optaplanner.test.impl.score.stream.ConstraintVerifier;

public class NQueensConstraintProviderTest {

    private final NQueensConstraintProvider constraintProvider = new NQueensConstraintProvider();
    private final ConstraintVerifier<NQueens> constraintVerifier = ConstraintVerifier.using(NQueens.class, Queen.class);

    private final Row row1 = new Row(0);
    private final Row row2 = new Row(1);
    private final Row row3 = new Row(2);
    private final Column column1 = new Column(0);
    private final Column column2 = new Column(1);
    private final Column column3 = new Column(2);

    @Test
    public void noHorizontalConflictWithOneQueen() {
        Queen queen1 = new Queen(0, row1, column1);
        constraintVerifier.forConstraint(constraintProvider::horizontalConflict)
                .given(queen1)
                .expectNoImpact();
    }

    @Test
    public void horizontalConflictWithTwoQueens() {
        Queen queen1 = new Queen(0, row1, column1);
        Queen queen2 = new Queen(1, row1, column2);
        constraintVerifier.forConstraint(constraintProvider::horizontalConflict)
                .given(queen1, queen2)
                .expectReward(1);
    }

    @Test
    public void horizontalConflictWithThreeQueens() {
        Queen queen1 = new Queen(0, row1, column1);
        Queen queen2 = new Queen(1, row1, column2);
        Queen queen3 = new Queen(2, row1, column3);
        constraintVerifier.forConstraint(constraintProvider::horizontalConflict)
                .given(queen1, queen2, queen3)
                .expectReward(3);
    }

    @Test
    public void noAscendingDiagonalConflictWithOneQueen() {
        Queen queen1 = new Queen(0, row1, column1);
        constraintVerifier.forConstraint(constraintProvider::ascendingDiagonalConflict)
                .given(queen1)
                .expectNoImpact();
    }

    @Test
    public void ascendingDiagonalConflictWithTwoQueens() {
        Queen queen1 = new Queen(0, row1, column2);
        Queen queen2 = new Queen(1, row2, column1);
        constraintVerifier.forConstraint(constraintProvider::ascendingDiagonalConflict)
                .given(queen1, queen2)
                .expectReward(1);
    }

    @Test
    public void ascendingDiagonalConflictWithThreeQueens() {
        Queen queen1 = new Queen(0, row1, column3);
        Queen queen2 = new Queen(1, row2, column2);
        Queen queen3 = new Queen(2, row3, column1);
        constraintVerifier.forConstraint(constraintProvider::ascendingDiagonalConflict)
                .given(queen1, queen2, queen3)
                .expectReward(3);
    }

    @Test
    public void noDescendingDiagonalConflictWithOneQueen() {
        Queen queen1 = new Queen(0, row1, column1);
        constraintVerifier.forConstraint(constraintProvider::descendingDiagonalConflict)
                .given(queen1)
                .expectNoImpact();
    }

    @Test
    public void descendingDiagonalConflictWithTwoQueens() {
        Queen queen1 = new Queen(0, row1, column1);
        Queen queen2 = new Queen(1, row2, column2);
        constraintVerifier.forConstraint(constraintProvider::descendingDiagonalConflict)
                .given(queen1, queen2)
                .expectReward(1);
    }

    @Test
    public void descendingDiagonalConflictWithThreeQueens() {
        Queen queen1 = new Queen(0, row1, column1);
        Queen queen2 = new Queen(1, row2, column2);
        Queen queen3 = new Queen(2, row3, column3);
        constraintVerifier.forConstraint(constraintProvider::descendingDiagonalConflict)
                .given(queen1, queen2, queen3)
                .expectReward(3);
    }

    private NQueens readSolution(String resource) throws IOException {
        Path tempFile = Files.createTempFile("optaplanner-test", "xml");
        try (InputStreamReader inputStreamReader =
                new InputStreamReader(NQueensConstraintProviderTest.class.getResourceAsStream(resource))) {
            List<String> lines = IOUtils.readLines(inputStreamReader);
            Files.write(tempFile,lines);
            final SolutionFileIO<NQueens> solutionFileIO = new XStreamSolutionFileIO<>(NQueens.class);
            return solutionFileIO.read(tempFile.toFile());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void constraintProviderIntegrationTest() throws IOException {
        constraintVerifier.forConstraintProvider(constraintProvider)
                .given(readSolution("256queens_-30.xml"))
                .expectScore(SimpleScore.of(-30));
    }
}
