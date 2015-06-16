package org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.solution;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueenEntity;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueensSolution;

public class TestdataQueenDifficultyWeightFactory implements
        SelectionSorterWeightFactory<TestdataQueensSolution, TestdataQueenEntity> {

    public Comparable createSorterWeight(TestdataQueensSolution nQueens, TestdataQueenEntity queen) {
        int distanceFromMiddle = calculateDistanceFromMiddle(nQueens.getN(), queen.getColumn());
        return new QueenDifficultyWeight(queen, distanceFromMiddle);
    }

    private static int calculateDistanceFromMiddle(int n, int columnIndex) {
        int middle = n / 2;
        int distanceFromMiddle = Math.abs(columnIndex - middle);
        if ((n % 2 == 0) && (columnIndex < middle)) {
            distanceFromMiddle--;
        }
        return distanceFromMiddle;
    }

    public static class QueenDifficultyWeight implements Comparable<QueenDifficultyWeight> {

        private final TestdataQueenEntity queen;
        private final int distanceFromMiddle;

        public QueenDifficultyWeight(TestdataQueenEntity queen, int distanceFromMiddle) {
            this.queen = queen;
            this.distanceFromMiddle = distanceFromMiddle;
        }

        public int compareTo(QueenDifficultyWeight other) {
            return new CompareToBuilder()
                    .append(other.distanceFromMiddle, distanceFromMiddle)
                    .append(queen.getColumn(), other.queen.getColumn()).toComparison();
        }
    }
}
