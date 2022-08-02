package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

final class TriangularNumbers {

    /**
     * Don't let anyone instantiate this class.
     */
    private TriangularNumbers() {
    }

    static int nth(int n) {
        return Math.multiplyExact(n, n + 1) / 2;
    }

    static double root(int x) {
        return (Math.sqrt(8 * x + 1) - 1) / 2;
    }

    static int floorRoot(int x) {
        return (int) Math.floor(root(x));
    }

    static int ceilRoot(int x) {
        return (int) Math.ceil(root(x));
    }
}
