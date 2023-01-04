package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.List;

import org.optaplanner.core.impl.domain.variable.supply.Supply;

public final class PillarSupply implements Supply {

    private final List<List<Object>> pillars;

    public PillarSupply(List<List<Object>> pillars) {
        this.pillars = pillars;
    }

    public List<List<Object>> getPillars() {
        return pillars;
    }
}
