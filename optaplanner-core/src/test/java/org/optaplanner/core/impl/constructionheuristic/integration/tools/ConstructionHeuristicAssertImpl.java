package org.optaplanner.core.impl.constructionheuristic.integration.tools;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Assertions for each tested construction heuristic
 */
public class ConstructionHeuristicAssertImpl {

    private static void checkSolutionSize(int n, List<ImmutablePair<Integer, Integer>> results) {
        assertEquals(n, results.size());
    }

    private static void checkPosition(ImmutablePair<Integer, Integer> assertPair, int x, int y) {
        assertEquals(x, assertPair.getRight().intValue());
        assertEquals(y, assertPair.getLeft().intValue());
    }

    public static class AssertFirstFit implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(final int n, final List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 0, 0);
            checkPosition(results.get(1), 1, 2);
            checkPosition(results.get(2), 2, 4);
            checkPosition(results.get(3), 3, 1);
            checkPosition(results.get(4), 4, 3);
            checkPosition(results.get(5), 5, 0);
            checkPosition(results.get(6), 6, 2);
            checkPosition(results.get(7), 7, 4);
        }
    }

    public static class AssertFirstFitDecreasing implements ConstructionHeuristicAssert {

        @Override
         public void assertSolution(final int n, final List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 4, 0);
            checkPosition(results.get(1), 3, 2);
            checkPosition(results.get(2), 5, 3);
            checkPosition(results.get(3), 2, 4);
            checkPosition(results.get(4), 6, 1);
            checkPosition(results.get(5), 1, 1);
            checkPosition(results.get(6), 7, 4);
            checkPosition(results.get(7), 0, 3);
        }
    }

    public static class AssertWeakestFit implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(final int n, final List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 0, 0);
            checkPosition(results.get(1), 1, 7);
            checkPosition(results.get(2), 2, 1);
            checkPosition(results.get(3), 3, 6);
            checkPosition(results.get(4), 4, 2);
            checkPosition(results.get(5), 5, 0);
            checkPosition(results.get(6), 6, 7);
            checkPosition(results.get(7), 7, 3);
        }
    }

    public static class AssertWeakestFitDecreasing implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(int n, List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 4, 0);
            checkPosition(results.get(1), 3, 7);
            checkPosition(results.get(2), 5, 6);
            checkPosition(results.get(3), 2, 1);
            checkPosition(results.get(4), 6, 3);
            checkPosition(results.get(5), 1, 4);
            checkPosition(results.get(6), 7, 5);
            checkPosition(results.get(7), 0, 2);
        }
    }

    public static class AssertStrongestFit implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(int n, List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 0, 4);
            checkPosition(results.get(1), 1, 2);
            checkPosition(results.get(2), 2, 5);
            checkPosition(results.get(3), 3, 3);
            checkPosition(results.get(4), 4, 6);
            checkPosition(results.get(5), 5, 0);
            checkPosition(results.get(6), 6, 3);
            checkPosition(results.get(7), 7, 1);
        }
    }

    public static class AssertStrongestFitDecreasing implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(int n, List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 4, 4);
            checkPosition(results.get(1), 3, 2);
            checkPosition(results.get(2), 5, 6);
            checkPosition(results.get(3), 2, 5);
            checkPosition(results.get(4), 6, 3);
            checkPosition(results.get(5), 1, 3);
            checkPosition(results.get(6), 7, 5);
            checkPosition(results.get(7), 0, 6);
        }
    }

    public static class AssertCheapestInsertion implements ConstructionHeuristicAssert {

        @Override
        public void assertSolution(int n, List<ImmutablePair<Integer, Integer>> results) {
            checkSolutionSize(n, results);

            checkPosition(results.get(0), 4, 0);
            checkPosition(results.get(1), 3, 7);
            checkPosition(results.get(2), 5, 6);
            checkPosition(results.get(3), 2, 1);
            checkPosition(results.get(4), 6, 3);
            checkPosition(results.get(5), 1, 4);
            checkPosition(results.get(6), 7, 5);
            checkPosition(results.get(7), 0, 2);
        }
    }

}
