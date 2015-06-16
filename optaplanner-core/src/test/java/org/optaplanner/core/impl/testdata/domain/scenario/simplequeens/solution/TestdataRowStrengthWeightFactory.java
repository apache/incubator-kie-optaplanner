package org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.solution;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.TestdataQueensSolution;

public class TestdataRowStrengthWeightFactory implements SelectionSorterWeightFactory<TestdataQueensSolution, Integer> {

    public Comparable createSorterWeight(TestdataQueensSolution nQueens, Integer row) {
        int distanceFromMiddle = calculateDistanceFromMiddle(nQueens.getN(), row);
        return new RowStrengthWeight(row, distanceFromMiddle);
    }

    private static int calculateDistanceFromMiddle(int n, int columnIndex) {
        int middle = n / 2;
        int distanceFromMiddle = Math.abs(columnIndex - middle);
        if ((n % 2 == 0) && (columnIndex < middle)) {
            distanceFromMiddle--;
        }
        return distanceFromMiddle;
    }

    public static class RowStrengthWeight implements Comparable<RowStrengthWeight> {

        private final Integer row;
        private final int distanceFromMiddle;

        public RowStrengthWeight(Integer row, int distanceFromMiddle) {
            this.row = row;
            this.distanceFromMiddle = distanceFromMiddle;
        }

        public int compareTo(RowStrengthWeight other) {
            return new CompareToBuilder()
                    .append(other.distanceFromMiddle, distanceFromMiddle)
                    .append(row, other.row).toComparison();
        }

    }
}
