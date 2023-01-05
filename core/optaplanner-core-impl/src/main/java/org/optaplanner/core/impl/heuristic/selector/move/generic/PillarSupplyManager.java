package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.domain.variable.supply.Supply;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;

public final class PillarSupplyManager implements SupplyManager {

    private final Map<Demand<PillarSupply>, PillarSupply> demandSupplyMap = new HashMap<>();

    @Override
    public <Supply_ extends Supply> Supply_ demand(Demand<Supply_> demand) {
        return (Supply_) demandSupplyMap.computeIfAbsent((Demand) demand, key -> key.createExternalizedSupply(this));
    }

    public <Supply_ extends Supply> void cancel(Demand<Supply_> demand) {
        demandSupplyMap.remove(demand);
    }

}
