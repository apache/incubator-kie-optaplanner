package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt.KOptListMove.getBetweenPredicate;
import static org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt.KOptListMove.getSuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
        KOptListMove<TestdataListSolution> kOptListMove = fromRemovedAndAddedEdges(variableDescriptor,
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

        KOptListMove<TestdataListSolution> kOptListMove = fromRemovedAndAddedEdges(variableDescriptor,
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

        KOptListMove<TestdataListSolution> kOptListMove = fromRemovedAndAddedEdges(variableDescriptor,
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

        KOptListMove<TestdataListSolution> kOptListMove = fromRemovedAndAddedEdges(variableDescriptor,
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

        kOptListMove = fromRemovedAndAddedEdges(variableDescriptor,
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

    /**
     * Create a sequential or non-sequential k-opt from the supplied pairs of undirected removed and added edges.
     *
     * @param listVariableDescriptor
     * @param indexVariableSupply
     * @param entity The entity
     * @param removedEdgeList The edges to remove. For each pair {@code (edgePairs[2*i], edgePairs[2*i+1])},
     *        it must be the case {@code edgePairs[2*i+1]} is either the successor or predecessor of
     *        {@code edgePairs[2*i]}. Additionally, each edge must belong to the given entity's
     *        list variable.
     * @param addedEdgeList The edges to add. Must contain only endpoints specified in the removedEdgeList.
     * @return A new sequential or non-sequential k-opt move with the specified undirected edges removed and added.
     * @param <Solution_>
     */
    private static <Solution_> KOptListMove<Solution_> fromRemovedAndAddedEdges(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            IndexVariableSupply indexVariableSupply,
            Object entity,
            List<Object> removedEdgeList,
            List<Object> addedEdgeList) {

        if (addedEdgeList.size() != removedEdgeList.size()) {
            throw new IllegalArgumentException(
                    "addedEdgeList (" + addedEdgeList + ") and removedEdgeList (" + removedEdgeList + ") have the same size");
        }

        if ((addedEdgeList.size() % 2) != 0) {
            throw new IllegalArgumentException(
                    "addedEdgeList and removedEdgeList are invalid: there is an odd number of endpoints.");
        }

        if (!addedEdgeList.containsAll(removedEdgeList)) {
            throw new IllegalArgumentException("addedEdgeList (" + addedEdgeList + ") is invalid; it contains endpoints "
                    + "that are not included in the removedEdgeList (" + removedEdgeList + ").");
        }

        Function<Object, Object> successorFunction =
                getSuccessorFunction(listVariableDescriptor, ignored -> entity, indexVariableSupply);

        for (int i = 0; i < removedEdgeList.size(); i += 2) {
            if (successorFunction.apply(removedEdgeList.get(i)) != removedEdgeList.get(i + 1)
                    && successorFunction.apply(removedEdgeList.get(i + 1)) != removedEdgeList.get(i)) {
                throw new IllegalArgumentException("removedEdgeList (" + removedEdgeList + ") contains an invalid edge ((" +
                        removedEdgeList.get(i) + ", " + removedEdgeList.get(i + 1) + ")).");
            }
        }

        Object[] tourArray = new Object[removedEdgeList.size() + 1];
        Integer[] incl = new Integer[removedEdgeList.size() + 1];
        for (int i = 0; i < removedEdgeList.size(); i += 2) {
            tourArray[i + 1] = removedEdgeList.get(i);
            tourArray[i + 2] = removedEdgeList.get(i + 1);
            int addedEdgeIndex = identityIndexOf(addedEdgeList, removedEdgeList.get(i));

            if (addedEdgeIndex % 2 == 0) {
                incl[i + 1] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex + 1)) + 1;
            } else {
                incl[i + 1] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex - 1)) + 1;
            }

            addedEdgeIndex = identityIndexOf(addedEdgeList, removedEdgeList.get(i + 1));
            if (addedEdgeIndex % 2 == 0) {
                incl[i + 2] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex + 1)) + 1;
            } else {
                incl[i + 2] = identityIndexOf(removedEdgeList, addedEdgeList.get(addedEdgeIndex - 1)) + 1;
            }
        }

        KOptDescriptor<Solution_> descriptor = new KOptDescriptor<>(tourArray,
                incl,
                getSuccessorFunction(listVariableDescriptor,
                        ignored -> entity,
                        indexVariableSupply),
                getBetweenPredicate(indexVariableSupply));
        return descriptor.getKOptListMove(listVariableDescriptor, indexVariableSupply, entity);
    }

    private static int identityIndexOf(List<Object> sourceList, Object query) {
        for (int i = 0; i < sourceList.size(); i++) {
            if (sourceList.get(i) == query) {
                return i;
            }
        }
        return -1;
    }

}
