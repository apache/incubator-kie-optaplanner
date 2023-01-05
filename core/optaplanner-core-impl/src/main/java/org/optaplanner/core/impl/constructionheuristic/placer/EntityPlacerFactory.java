package org.optaplanner.core.impl.constructionheuristic.placer;

import org.optaplanner.core.config.constructionheuristic.placer.EntityPlacerConfig;
import org.optaplanner.core.config.constructionheuristic.placer.PooledEntityPlacerConfig;
import org.optaplanner.core.config.constructionheuristic.placer.QueuedEntityPlacerConfig;
import org.optaplanner.core.config.constructionheuristic.placer.QueuedValuePlacerConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.solver.ClassInstanceCache;

public interface EntityPlacerFactory<Solution_> {

    static <Solution_> EntityPlacerFactory<Solution_> create(EntityPlacerConfig<?> entityPlacerConfig,
            ClassInstanceCache instanceCache) {
        if (PooledEntityPlacerConfig.class.isAssignableFrom(entityPlacerConfig.getClass())) {
            return new PooledEntityPlacerFactory<>((PooledEntityPlacerConfig) entityPlacerConfig, instanceCache);
        } else if (QueuedEntityPlacerConfig.class.isAssignableFrom(entityPlacerConfig.getClass())) {
            return new QueuedEntityPlacerFactory<>((QueuedEntityPlacerConfig) entityPlacerConfig, instanceCache);
        } else if (QueuedValuePlacerConfig.class.isAssignableFrom(entityPlacerConfig.getClass())) {
            return new QueuedValuePlacerFactory<>((QueuedValuePlacerConfig) entityPlacerConfig, instanceCache);
        } else {
            throw new IllegalArgumentException(String.format("Unknown %s type: (%s).",
                    EntityPlacerConfig.class.getSimpleName(), entityPlacerConfig.getClass().getName()));
        }
    }

    EntityPlacer<Solution_> buildEntityPlacer(HeuristicConfigPolicy<Solution_> configPolicy);
}
