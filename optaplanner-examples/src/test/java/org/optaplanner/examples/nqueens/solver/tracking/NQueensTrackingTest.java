package org.optaplanner.examples.nqueens.solver.tracking;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NQueensTrackingTest {

    protected void assertTrackingList(List<NQueensStepTracking> expected, List<NQueensStepTracking> recorded) {
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getColumnIndex(), recorded.get(i).getColumnIndex());
            assertEquals(expected.get(i).getRowIndex(), recorded.get(i).getRowIndex());
        }
    }
}
