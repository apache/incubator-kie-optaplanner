package org.optaplanner.core.impl.heuristic.selector.move.generic;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

public class InitializedSelectionFilter<Solution_> implements SelectionFilter<Solution_, Object> {

    private final EntityDescriptor<Solution_> entityDescriptor;

    InitializedSelectionFilter(EntityDescriptor<Solution_> entityDescriptor) {
        this.entityDescriptor = entityDescriptor;
    }

    @Override
    public boolean accept(ScoreDirector<Solution_> scoreDirector, Object entity) {
        return entityDescriptor.hasAnyNoNullAndNoEmptyGenuineAndShadowVariables(entity);
    }
}