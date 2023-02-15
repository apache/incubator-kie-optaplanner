package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt.KOptUtils.getBetweenPredicate;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

public class KOptDescriptorTest {

    private final TestdataListValue v1 = new TestdataListValue("1");
    private final TestdataListValue v2 = new TestdataListValue("2");
    private final TestdataListValue v3 = new TestdataListValue("3");
    private final TestdataListValue v4 = new TestdataListValue("4");
    private final TestdataListValue v5 = new TestdataListValue("5");
    private final TestdataListValue v6 = new TestdataListValue("6");
    private final TestdataListValue v7 = new TestdataListValue("7");
    private final TestdataListValue v8 = new TestdataListValue("8");
    private final TestdataListValue v9 = new TestdataListValue("9");
    private final TestdataListValue v10 = new TestdataListValue("10");
    private final TestdataListValue v11 = new TestdataListValue("11");
    private final TestdataListValue v12 = new TestdataListValue("12");

    @Test
    public void testGetCyclesForPermutationOneCycle() {
        List<TestdataListValue> originalTour = List.of(v1, v2, v3, v4, v5, v6, v7, v8);
        List<TestdataListValue> removedEdges = List.of(v1, v2,
                v3, v4,
                v5, v6,
                v7, v8);
        List<TestdataListValue> addedEdges = List.of(v1, v4,
                v2, v7,
                v5, v3,
                v6, v8);

        KOptDescriptor<?, TestdataListValue> kOptDescriptor = fromRemovedAndAddedEdges(originalTour,
                removedEdges,
                addedEdges);
        KOptCycleInfo cycleInfo = KOptUtils.getCyclesForPermutation(kOptDescriptor);
        assertThat(cycleInfo.cycleCount).isEqualTo(1);

        // Cycles:
        // v1 -> v4 -> v5 -> v3 -> v2 -> v7 -> v6 -> v8
        assertThat(cycleInfo.indexToCycleIdentifier).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testGetCyclesForPermutationTwoCycle() {
        List<TestdataListValue> originalTour = List.of(v1, v2, v3, v4, v5, v6, v7, v8);
        List<TestdataListValue> removedEdges = List.of(v1, v2,
                v3, v4,
                v5, v6,
                v7, v8);
        List<TestdataListValue> addedEdges = List.of(v1, v4,
                v3, v6,
                v5, v8,
                v7, v2);

        KOptDescriptor<?, TestdataListValue> kOptDescriptor = fromRemovedAndAddedEdges(originalTour,
                removedEdges,
                addedEdges);
        KOptCycleInfo cycleInfo = KOptUtils.getCyclesForPermutation(kOptDescriptor);
        assertThat(cycleInfo.cycleCount).isEqualTo(2);

        // Cycles:
        // v1 -> v4 -> v5 -> v8
        // v2 -> v3 -> v6 -> v7
        assertThat(cycleInfo.indexToCycleIdentifier).containsExactly(0, 0, 1, 1, 0, 0, 1, 1, 0);
    }

    @Test
    public void testGetCyclesForPermutationThreeCycle() {
        List<TestdataListValue> originalTour = List.of(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
        List<TestdataListValue> removedEdges = List.of(v1, v2,
                v3, v4,
                v5, v6,
                v7, v8,
                v9, v10,
                v11, v12);
        List<TestdataListValue> addedEdges = List.of(v1, v4,
                v5, v12,
                v3, v6,
                v7, v2,
                v8, v10,
                v11, v9);

        KOptDescriptor<?, TestdataListValue> kOptDescriptor = fromRemovedAndAddedEdges(originalTour,
                removedEdges,
                addedEdges);
        KOptCycleInfo cycleInfo = KOptUtils.getCyclesForPermutation(kOptDescriptor);
        assertThat(cycleInfo.cycleCount).isEqualTo(3);

        // Cycles:
        // v1 -> v4 -> v5 -> v12
        // v2 -> v7 -> v6 -> v3
        // v8 -> v10 -> v11 -> v9
        assertThat(cycleInfo.indexToCycleIdentifier).containsExactly(0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 0);
    }

    private static <Solution_> KOptDescriptor<Solution_, TestdataListValue> fromRemovedAndAddedEdges(
            List<TestdataListValue> originalTour,
            List<TestdataListValue> removedEdgeList,
            List<TestdataListValue> addedEdgeList) {

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

        Function<TestdataListValue, TestdataListValue> successorFunction =
                item -> originalTour.get((originalTour.indexOf(item) + 1) % originalTour.size());

        for (int i = 0; i < removedEdgeList.size(); i += 2) {
            if (successorFunction.apply(removedEdgeList.get(i)) != removedEdgeList.get(i + 1)
                    && successorFunction.apply(removedEdgeList.get(i + 1)) != removedEdgeList.get(i)) {
                throw new IllegalArgumentException("removedEdgeList (" + removedEdgeList + ") contains an invalid edge ((" +
                        removedEdgeList.get(i) + ", " + removedEdgeList.get(i + 1) + ")).");
            }
        }

        TestdataListValue[] tourArray = new TestdataListValue[removedEdgeList.size() + 1];
        int[] incl = new int[removedEdgeList.size() + 1];
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

        return new KOptDescriptor<>(tourArray,
                incl,
                item -> originalTour.get((originalTour.indexOf(item) + 1) % originalTour.size()),
                getBetweenPredicate(originalTour::indexOf));
    }

    private static int identityIndexOf(List<TestdataListValue> sourceList, TestdataListValue query) {
        for (int i = 0; i < sourceList.size(); i++) {
            if (sourceList.get(i) == query) {
                return i;
            }
        }
        return -1;
    }
}
