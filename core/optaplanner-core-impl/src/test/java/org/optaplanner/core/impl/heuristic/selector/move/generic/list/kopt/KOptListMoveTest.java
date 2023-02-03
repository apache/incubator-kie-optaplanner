package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableListener;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

class KOptListMoveTest {
    private final SolutionDescriptor<TestdataListSolution> solutionDescriptor = TestdataListSolution.buildSolutionDescriptor();
    private final InnerScoreDirector<TestdataListSolution, ?> scoreDirector =
            PlannerTestUtils.mockScoreDirector(solutionDescriptor);
    private final ListVariableDescriptor<TestdataListSolution> variableDescriptor =
            solutionDescriptor.getListVariableDescriptors().get(0);

    private final TestdataListValue v1 = new TestdataListValue("1");
    private final TestdataListValue v2 = new TestdataListValue("2");
    private final TestdataListValue v3 = new TestdataListValue("3");
    private final TestdataListValue v4 = new TestdataListValue("4");
    private final TestdataListValue v5 = new TestdataListValue("5");
    private final TestdataListValue v6 = new TestdataListValue("6");
    private final TestdataListValue v7 = new TestdataListValue("7");
    private final TestdataListValue v8 = new TestdataListValue("8");

    @Test
    void test3Opt() {
        IndexVariableSupply indexVariableSupply =
                scoreDirector.getSupplyManager().demand(new IndexVariableDemand<>(variableDescriptor));
        IndexVariableListener indexVariableListener = (IndexVariableListener) indexVariableSupply;
        TestdataListEntity e1 = new TestdataListEntity("e1", new ArrayList<>(List.of(v1, v2, v3, v4, v5, v6)));
        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 6);
        KOptListMove<TestdataListSolution> kOptListMove = KOptListMove.fromRemovedAndAddedEdges(variableDescriptor,
                indexVariableSupply,
                e1,
                List.of(v6, v1,
                        v2, v3,
                        v4, v5),
                List.of(v1, v3,
                        v2, v5,
                        v4, v6));

        assertThat(kOptListMove.isMoveDoable(scoreDirector)).isTrue();
        AbstractMove<TestdataListSolution> undoMove = kOptListMove.doMove(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v1, v2, v5, v6, v4, v3);
        undoMove.doMoveOnly(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4, v5, v6);
    }

    @Test
    void test4Opt() {
        IndexVariableSupply indexVariableSupply =
                scoreDirector.getSupplyManager().demand(new IndexVariableDemand<>(variableDescriptor));
        IndexVariableListener indexVariableListener = (IndexVariableListener) indexVariableSupply;
        TestdataListEntity e1 = new TestdataListEntity("e1", new ArrayList<>(
                List.of(v1, v2, v3, v4, v5, v6, v7, v8)));

        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);

        KOptListMove<TestdataListSolution> kOptListMove = KOptListMove.fromRemovedAndAddedEdges(variableDescriptor,
                indexVariableSupply,
                e1,
                List.of(
                        v1, v2,
                        v4, v3,
                        v7, v8,
                        v6, v5),
                List.of(
                        v2, v4,
                        v3, v7,
                        v8, v6,
                        v5, v1));

        assertThat(kOptListMove.isMoveDoable(scoreDirector)).isTrue();
        AbstractMove<TestdataListSolution> undoMove = kOptListMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v5, v4, v2, v3, v7, v6, v8);

        undoMove.doMoveOnly(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    @Test
    void testDoubleBridge4Opt() {
        IndexVariableSupply indexVariableSupply =
                scoreDirector.getSupplyManager().demand(new IndexVariableDemand<>(variableDescriptor));
        IndexVariableListener indexVariableListener = (IndexVariableListener) indexVariableSupply;
        TestdataListEntity e1 = new TestdataListEntity("e1", new ArrayList<>(
                List.of(v1, v2, v3, v4, v5, v6, v7, v8)));

        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);

        KOptListMove<TestdataListSolution> kOptListMove = KOptListMove.fromRemovedAndAddedEdges(variableDescriptor,
                indexVariableSupply,
                e1,
                List.of(v8, v1,
                        v4, v5,
                        v2, v3,
                        v6, v7),
                List.of(
                        v1, v4,
                        v6, v3,
                        v5, v8,
                        v7, v2));

        assertThat(kOptListMove.isMoveDoable(scoreDirector)).isTrue();
        AbstractMove<TestdataListSolution> undoMove = kOptListMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v7, v8, v5, v6, v3, v4);

        undoMove.doMoveOnly(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    @Test
    void testIsFeasible() {
        IndexVariableSupply indexVariableSupply =
                scoreDirector.getSupplyManager().demand(new IndexVariableDemand<>(variableDescriptor));
        IndexVariableListener indexVariableListener = (IndexVariableListener) indexVariableSupply;
        TestdataListEntity e1 = new TestdataListEntity("e1", new ArrayList<>(
                List.of(v1, v2, v3, v4, v5, v6, v7, v8)));

        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);

        KOptListMove<TestdataListSolution> kOptListMove = KOptListMove.fromRemovedAndAddedEdges(variableDescriptor,
                indexVariableSupply,
                e1,
                List.of(
                        v1, v2,
                        v4, v3,
                        v7, v8,
                        v6, v5),
                List.of(
                        v2, v4,
                        v3, v7,
                        v8, v6,
                        v5, v1));
        // this move create 1 cycle (v1 -> v5 -> v4 -> v2 -> v3 -> v7 -> v6 -> v8 -> v1 -> ...)
        assertThat(kOptListMove.isMoveDoable(scoreDirector)).isTrue();

        e1 = new TestdataListEntity("e1", new ArrayList<>(
                List.of(v1, v2, v3, v4, v8, v7, v5, v6)));

        indexVariableListener.afterListVariableChanged(scoreDirector, e1, 0, 8);

        kOptListMove = KOptListMove.fromRemovedAndAddedEdges(variableDescriptor,
                indexVariableSupply,
                e1,
                List.of(
                        v1, v2,
                        v3, v4,
                        v7, v8,
                        v6, v5),
                List.of(
                        v2, v3,
                        v4, v7,
                        v8, v6,
                        v5, v1));
        // this move create 2 cycles (v2...v3->t2...) and (v4...v5->v7...v6->v1...v8->v4...)
        assertThat(kOptListMove.isMoveDoable(scoreDirector)).isFalse();
    }
}
