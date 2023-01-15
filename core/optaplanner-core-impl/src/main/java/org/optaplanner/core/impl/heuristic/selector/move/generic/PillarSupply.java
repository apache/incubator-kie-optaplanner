package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.List;

import org.optaplanner.core.impl.domain.variable.supply.Supply;

@FunctionalInterface
public interface PillarSupply extends Supply {

    List<List<Object>> getPillars();

}
