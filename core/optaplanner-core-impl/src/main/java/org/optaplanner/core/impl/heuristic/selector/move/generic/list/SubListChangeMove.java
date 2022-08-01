package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SubListChangeMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final Object sourceEntity;
    private final int sourceIndex;
    private final int length;
    private final Object destinationEntity;
    private final int destinationIndex;

    public SubListChangeMove(
            ListVariableDescriptor<Solution_> variableDescriptor,
            Object sourceEntity, int sourceIndex, int length,
            Object destinationEntity, int destinationIndex) {
        this.variableDescriptor = variableDescriptor;
        this.sourceEntity = sourceEntity;
        this.sourceIndex = sourceIndex;
        this.length = length;
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new SubListChangeMove<>(variableDescriptor, destinationEntity, destinationIndex, length, sourceEntity,
                sourceIndex);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        List<Object> sourceList = variableDescriptor.getListVariable(sourceEntity);
        List<Object> subList = sourceList.subList(sourceIndex, sourceIndex + length);

        variableDescriptor.getListVariable(destinationEntity).addAll(destinationIndex, subList);
        sourceList.removeAll(subList);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return destinationEntity != sourceEntity
                || destinationIndex + length <= variableDescriptor.getListSize(destinationEntity)
                        && destinationIndex != sourceIndex;
    }

    @Override
    public String toString() {
        return String.format("|%d| {%s[%d..%d] -> %s[%d]}",
                length, sourceEntity, sourceIndex, sourceIndex + length, destinationEntity, destinationIndex);
    }
}
