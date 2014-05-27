package org.optaplanner.examples.vehiclerouting.domain;

/**
 *
 * CostMatrix gives the edge cost between two locations.
 *
 * Where the cost of the  edge leading from A to B is the same as the edge from B to A.
 * A and A has 0 cost.
 *
 * The matrix is sparse matrix, for size 5x5, there is values only in:
 *
 *      x coordinate
 *
 * |Z| | | | |
 * |X|Z| | | |
 * |X|X|Z| | |  y coordinate
 * |X|X|X|Z| |
 * |X|X|X|X|Z|
 */
public class CostMatrix {

    private int costs[][];

    public CostMatrix(int size, int maxValue)  {
        costs = new int[size][];
        for (int y = 0; y < size; y++) {
            costs[y] = new int[y+1];
            for(int x = 0; x < y; x++) {
                costs[y][x] = maxValue;
            }
        }
    }

    public int get(int x, int y) {
        if (x == y) {
            return 0;
        } else if (x > y) {
            return get(y, x);
        } else {
            return costs[y][x];
        }
    }

    public void set(int x, int y, int cost) {
        if (x > y) {
            set(y, x, cost);
        } else {
            costs[y][x] = cost;
        }
    }
}
