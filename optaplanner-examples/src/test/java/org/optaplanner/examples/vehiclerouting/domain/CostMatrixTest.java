package org.optaplanner.examples.vehiclerouting.domain;

import junit.framework.TestCase;
import org.junit.Test;
import org.optaplanner.examples.tsp.domain.Domicile;

/**
 */
public class CostMatrixTest extends TestCase {
    final int maxValue = 26843545;

    @Test
    public void testEmptyMatrix() {
        final int size = 5;
        CostMatrix costMatrix = new CostMatrix(size, maxValue);

        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                if (x == y) {
                    assertEquals(0, costMatrix.get(x,y));
                } else {
                    assertEquals(maxValue, costMatrix.get(x, y));
                }
            }
        }
    }

    @Test
    public void testMatrix() {
        final int size = 5;
        CostMatrix costMatrix = new CostMatrix(size, maxValue);
        costMatrix.set(3, 2, 12);
        assertEquals(12, costMatrix.get(3, 2));
        assertEquals(12, costMatrix.get(2, 3));


        costMatrix.set(1, 2, 44);
        assertEquals(44, costMatrix.get(1, 2));
        assertEquals(44, costMatrix.get(2, 1));

    }
}
