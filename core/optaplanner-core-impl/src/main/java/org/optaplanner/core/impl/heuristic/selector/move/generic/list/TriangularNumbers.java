package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

final class TriangularNumbers {

    /**
     * Don't let anyone instantiate this class.
     */
    private TriangularNumbers() {
    }

    static int nthTriangle(int n) {
        return Math.multiplyExact(n, n + 1) / 2;
    }

    static double triangularRoot(int x) {
        return (Math.sqrt(8 * x + 1) - 1) / 2;
    }
}
