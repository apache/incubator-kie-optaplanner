package org.optaplanner.core.impl.testdata.domain.scenario.simplequeens;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.solution.TestdataQueenDifficultyWeightFactory;
import org.optaplanner.core.impl.testdata.domain.scenario.simplequeens.solution.TestdataRowStrengthWeightFactory;

@PlanningEntity(difficultyWeightFactoryClass = TestdataQueenDifficultyWeightFactory.class)
public class TestdataQueenEntity extends TestdataEntity {

    private Integer column;
    private Integer row;

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @PlanningVariable(strengthWeightFactoryClass = TestdataRowStrengthWeightFactory.class,
            valueRangeProviderRefs = { "rowList" })
    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public int getAscendingDiagonalIndex() {
        return (column + row);
    }

    public int getDescendingDiagonalIndex() {
        return (column - row);
    }

}
