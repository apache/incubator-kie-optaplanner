package org.optaplanner.examples.nurserostering.domain;

import java.util.Comparator;

public class ShiftAssignmentComparator implements Comparator<ShiftAssignment> {

    private static final Comparator<ShiftAssignment> COMPARATOR =
            Comparator.comparing((ShiftAssignment a) -> a.getShiftDate().getDate())
            .thenComparing(ShiftAssignment::getIndexInShift);

    @Override
    public int compare(ShiftAssignment o1, ShiftAssignment o2) {
        return COMPARATOR.compare(o1, o2);
    }
}
