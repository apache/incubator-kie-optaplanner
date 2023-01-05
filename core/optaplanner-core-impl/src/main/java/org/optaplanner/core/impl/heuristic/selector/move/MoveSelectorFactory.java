package org.optaplanner.core.impl.heuristic.selector.move;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveIteratorFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveListFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.KOptMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.move.composite.CartesianProductMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.composite.UnionMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactoryFactory;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactoryFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.KOptMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.SubListChangeMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.SubListSwapMoveSelectorFactory;
import org.optaplanner.core.impl.solver.ClassInstanceCache;

public interface MoveSelectorFactory<Solution_> {

    static <Solution_> MoveSelectorFactory<Solution_> create(MoveSelectorConfig<?> moveSelectorConfig,
            ClassInstanceCache instanceCache) {
        if (ChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new ChangeMoveSelectorFactory<>((ChangeMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (SwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SwapMoveSelectorFactory<>((SwapMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (PillarChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new PillarChangeMoveSelectorFactory<>((PillarChangeMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (PillarSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new PillarSwapMoveSelectorFactory<>((PillarSwapMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (UnionMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new UnionMoveSelectorFactory<>((UnionMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (CartesianProductMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new CartesianProductMoveSelectorFactory<>((CartesianProductMoveSelectorConfig) moveSelectorConfig,
                    instanceCache);
        } else if (SubListChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubListChangeMoveSelectorFactory<>((SubListChangeMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (SubListSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubListSwapMoveSelectorFactory<>((SubListSwapMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (SubChainChangeMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubChainChangeMoveSelectorFactory<>((SubChainChangeMoveSelectorConfig) moveSelectorConfig,
                    instanceCache);
        } else if (SubChainSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new SubChainSwapMoveSelectorFactory<>((SubChainSwapMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (TailChainSwapMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new TailChainSwapMoveSelectorFactory<>((TailChainSwapMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else if (MoveIteratorFactoryConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new MoveIteratorFactoryFactory<>((MoveIteratorFactoryConfig) moveSelectorConfig, instanceCache);
        } else if (MoveListFactoryConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new MoveListFactoryFactory<>((MoveListFactoryConfig) moveSelectorConfig, instanceCache);
        } else if (KOptMoveSelectorConfig.class.isAssignableFrom(moveSelectorConfig.getClass())) {
            return new KOptMoveSelectorFactory<>((KOptMoveSelectorConfig) moveSelectorConfig, instanceCache);
        } else {
            throw new IllegalArgumentException(String.format("Unknown %s type: (%s).",
                    MoveSelectorConfig.class.getSimpleName(), moveSelectorConfig.getClass().getName()));
        }
    }

    /**
     * Builds {@link MoveSelector} from the {@link MoveSelectorConfig} and provided parameters.
     *
     * @param configPolicy never null
     * @param minimumCacheType never null, If caching is used (different from {@link SelectionCacheType#JUST_IN_TIME}),
     *        then it should be at least this {@link SelectionCacheType} because an ancestor already uses such caching
     *        and less would be pointless.
     * @param inheritedSelectionOrder never null
     * @return never null
     */
    MoveSelector<Solution_> buildMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder);
}
