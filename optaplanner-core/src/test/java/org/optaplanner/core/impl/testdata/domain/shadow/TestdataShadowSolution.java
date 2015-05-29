package org.optaplanner.core.impl.testdata.domain.shadow;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;

import java.util.Collection;
import java.util.List;

@PlanningSolution
public class TestdataShadowSolution extends TestdataObject implements Solution<SimpleScore> {

    private List<TestdataShadowEntity> entities;
    private List<TestdataShadowAnchor> anchors;

    private SimpleScore score;

    public static TestdataShadowSolution createChainedSortingSolution(List<TestdataShadowEntity> entities,
                                                                      List<TestdataShadowAnchor> anchors) {
        TestdataShadowSolution newOne = new TestdataShadowSolution();
        newOne.entities = entities;
        newOne.anchors = anchors;
        return newOne;
    }

    @Override
    public SimpleScore getScore() {
        return score;
    }

    @Override
    public void setScore(SimpleScore score) {
        this.score = score;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        return anchors;
    }

    @ValueRangeProvider(id = "anchorList")
    public List<TestdataShadowAnchor> getAnchorList() {
        return anchors;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "entityList")
    public List<TestdataShadowEntity> getEntityList() {
        return entities;
    }

    public void setEntities(List<TestdataShadowEntity> entities) {
        this.entities = entities;
    }

    public void setAnchors(List<TestdataShadowAnchor> anchors) {
        this.anchors = anchors;
    }
}
