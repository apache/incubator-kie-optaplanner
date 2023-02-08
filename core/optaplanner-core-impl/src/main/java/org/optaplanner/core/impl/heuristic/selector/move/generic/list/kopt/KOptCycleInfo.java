package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

/**
 * Describes the minimal amount of cycles a permutation can be expressed as
 * and provide a mapping of removed edge endpoint index to cycle identifier
 * (where all indices that are in the same k-cycle have the same identifier).
 */
public class KOptCycleInfo {

    /**
     * The total number of k-cycles in the permutation. This is one more than the
     * maximal value in {@link KOptCycleInfo#indexToCycleIdentifier}.
     */
    public final int cycleCount;

    /**
     * Maps an index in the removed endpoints to the cycle it belongs to
     * after the new edges are added. Ranges from 0 to {@link #cycleCount} - 1.
     */
    public final Integer[] indexToCycleIdentifier;

    public KOptCycleInfo(int cycleCount, Integer[] indexToCycleIdentifier) {
        this.cycleCount = cycleCount;
        this.indexToCycleIdentifier = indexToCycleIdentifier;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Cycles(cycleCount=").append(cycleCount).append(",\n");
        out.append("indexToCycleNum=[");
        for (int i = 1; i < indexToCycleIdentifier.length; i++) {
            out.append(indexToCycleIdentifier[i]).append(", ");
        }
        out.delete(out.length() - 2, out.length()).append("]\n");
        return out.toString();
    }
}
