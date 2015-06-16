package org.optaplanner.core.impl.testdata.domain.scenario.simplequeens;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import java.util.Collection;
import java.util.List;

/**
 * for test purposes only
 */

@PlanningSolution
public class TestdataQueensSolution implements Solution<SimpleScore> {

    private int n;

    private List<Integer> columnList;
    private List<Integer> rowList;

    private List<TestdataQueenEntity> testdataQueenEntityList;

    private SimpleScore score;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public List<Integer> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Integer> columnList) {
        this.columnList = columnList;
    }

    @ValueRangeProvider(id = "rowList")
    public List<Integer> getRowList() {
        return rowList;
    }

    public void setRowList(List<Integer> rowList) {
        this.rowList = rowList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataQueenEntity> getTestdataQueenEntityList() {
        return testdataQueenEntityList;
    }

    public void setTestdataQueenEntityList(List<TestdataQueenEntity> testdataQueenEntityList) {
        this.testdataQueenEntityList = testdataQueenEntityList;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    public Collection<? extends Object> getProblemFacts() {
        return null;
    }

}
