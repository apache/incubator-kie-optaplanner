/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.examples.nqueens.domain.NQueens;
import org.optaplanner.examples.nqueens.domain.Queen;

public class NQueensMapBasedEasyScoreCalculator implements EasyScoreCalculator<NQueens> {

    public SimpleScore calculateScore(NQueens nQueens) {
        int n = nQueens.getN();
        List<Queen> queenList = nQueens.getQueenList();

        Map<Integer, Integer> rowIndexCountMap = new HashMap<Integer, Integer>(n);
        Map<Integer, Integer> ascendingDiagonalIndexCountMap = new HashMap<Integer, Integer>(n);
        Map<Integer, Integer> descendingDiagonalIndexCountMap = new HashMap<Integer, Integer>(n);
        int score = 0;
        for (Queen queen : queenList) {
            if (queen.getRow() != null) {
                int rowIndex = queen.getRowIndex();
                Integer rowIndexCount = rowIndexCountMap.get(rowIndex);
                if (rowIndexCount != null) {
                    score -= rowIndexCount;
                    rowIndexCount++;
                } else {
                    rowIndexCount = 1;
                }
                rowIndexCountMap.put(rowIndex, rowIndexCount);

                int ascendingDiagonalIndex = queen.getAscendingDiagonalIndex();
                Integer ascendingDiagonalIndexCount = ascendingDiagonalIndexCountMap.get(ascendingDiagonalIndex);
                if (ascendingDiagonalIndexCount != null) {
                    score -= ascendingDiagonalIndexCount;
                    ascendingDiagonalIndexCount++;
                } else {
                    ascendingDiagonalIndexCount = 1;
                }
                ascendingDiagonalIndexCountMap.put(ascendingDiagonalIndex, ascendingDiagonalIndexCount);

                int descendingDiagonalIndex = queen.getDescendingDiagonalIndex();
                Integer descendingDiagonalIndexCount = descendingDiagonalIndexCountMap.get(descendingDiagonalIndex);
                if (descendingDiagonalIndexCount != null) {
                    score -= descendingDiagonalIndexCount;
                    descendingDiagonalIndexCount++;
                } else {
                    descendingDiagonalIndexCount = 1;
                }
                descendingDiagonalIndexCountMap.put(descendingDiagonalIndex, descendingDiagonalIndexCount);
            }
        }
        return SimpleScore.valueOf(score);
    }

}
