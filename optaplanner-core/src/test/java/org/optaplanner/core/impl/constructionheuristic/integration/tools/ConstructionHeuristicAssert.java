package org.optaplanner.core.impl.constructionheuristic.integration.tools;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

/**
 * To enable assertion of construction heuristic test in single common way.
 */
public interface ConstructionHeuristicAssert {

    /**
     * Assertion method.
     * 
     * @param n
     *            dimension
     * @param results
     *            list of pairs [row, column] in each step of solving
     */
    void assertSolution(final int n, final List<ImmutablePair<Integer, Integer>> results);

}
