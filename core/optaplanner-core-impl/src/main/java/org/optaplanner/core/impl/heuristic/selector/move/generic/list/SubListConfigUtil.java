package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.SubListSelectorConfig;
import org.slf4j.LoggerFactory;

final class SubListConfigUtil {

    private SubListConfigUtil() {
    }

    static void transferDeprecatedProperty(
            String propertyName,
            SubListChangeMoveSelectorConfig subListChangeMoveSelectorConfig,
            Function<SubListChangeMoveSelectorConfig, Integer> sourceGetter,
            SubListSelectorConfig subListSelectorConfig,
            Function<SubListSelectorConfig, Integer> targetGetter,
            BiConsumer<SubListSelectorConfig, Integer> targetSetter) {
        Integer moveSelectorSubListSize = sourceGetter.apply(subListChangeMoveSelectorConfig);
        if (moveSelectorSubListSize != null) {
            LoggerFactory.getLogger(subListChangeMoveSelectorConfig.getClass()).warn(
                    "{}'s {} property is deprecated. Set {} on the child {}.",
                    subListChangeMoveSelectorConfig.getClass().getSimpleName(), propertyName,
                    propertyName, SubListSelectorConfig.class.getSimpleName());
            Integer subListSize = targetGetter.apply(subListSelectorConfig);
            if (subListSize != null) {
                throw new IllegalArgumentException("The subListChangeMoveSelector (" + subListChangeMoveSelectorConfig
                        + ") and its child subListSelector (" + subListSelectorConfig
                        + ") both set the " + propertyName
                        + ", which is a conflict.\n"
                        + "Use " + SubListSelectorConfig.class.getSimpleName() + "." + propertyName + " only.");
            }
            targetSetter.accept(subListSelectorConfig, moveSelectorSubListSize);
        }
    }
}
