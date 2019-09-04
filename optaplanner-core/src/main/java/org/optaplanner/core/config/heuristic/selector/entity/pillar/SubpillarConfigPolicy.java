package org.optaplanner.core.config.heuristic.selector.entity.pillar;

public final class SubpillarConfigPolicy {

    public static SubpillarConfigPolicy withoutSubpillars() {
        return new SubpillarConfigPolicy();
    }

    public static SubpillarConfigPolicy withSubpillars(int minSize, int maxSize) {
        return new SubpillarConfigPolicy(minSize, maxSize);
    }

    public static SubpillarConfigPolicy withSubpillarsUnlimited() {
        return withSubpillars(1, Integer.MAX_VALUE);
    }

    private final boolean subPillarEnabled;
    private final int minSubpillarSize;
    private final int maxSubpillarSize;

    private SubpillarConfigPolicy(int minimumSubPillarSize, int maximumSubPillarSize) {
        this.subPillarEnabled = true;
        if (minimumSubPillarSize < 1) {
            throw new IllegalStateException("The selector (" + this
                    + ")'s minimumPillarSize (" + minimumSubPillarSize
                    + ") must be at least 1.");
        }
        if (minimumSubPillarSize > maximumSubPillarSize) {
            throw new IllegalStateException("The minimumPillarSize (" + minimumSubPillarSize
                    + ") must be at least maximumSubChainSize (" + maximumSubPillarSize + ").");
        }

        this.minSubpillarSize = minimumSubPillarSize;
        this.maxSubpillarSize = maximumSubPillarSize;
    }

    private SubpillarConfigPolicy() {
        this.subPillarEnabled = false;
        this.minSubpillarSize = -1;
        this.maxSubpillarSize = -1;
    }

    public boolean isSubPillarEnabled() {
        return subPillarEnabled;
    }

    public int getMinSubpillarSize() {
        return minSubpillarSize;
    }

    public int getMaxSubpillarSize() {
        return maxSubpillarSize;
    }
}
