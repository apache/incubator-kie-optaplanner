package org.optaplanner.core.config.heuristic.selector.move.generic;

import java.util.Comparator;

public enum PillarType {

    /**
     * Pillars will only be affected in their entirety, in the order in which they're constructed.
     */
    FULL_ONLY,
    /**
     * Pillars may also be affected partially, but still only in the order in which they're constructed.
     */
    FULL_AND_SUB,
    /**
     * Pillars may also be affected partially, before which they will be reordered to match a given {@link Comparator}.
     */
    SEQUENTIAL;
}
