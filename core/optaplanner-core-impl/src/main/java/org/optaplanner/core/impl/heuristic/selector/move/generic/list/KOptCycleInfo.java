package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

public class KOptCycleInfo {
    public final int cycleCount;
    public final Integer[] indexToCycle;

    public KOptCycleInfo(int cycleCount, Integer[] indexToCycle) {
        this.cycleCount = cycleCount;
        this.indexToCycle = indexToCycle;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Cycles(cycleCount=").append(cycleCount).append(",\n");
        out.append("indexToCycleNum=[");
        for (int i = 1; i < indexToCycle.length; i++) {
            out.append(indexToCycle[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");
        return out.toString();
    }
}
