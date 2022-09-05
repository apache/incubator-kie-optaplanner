package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

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
    private final boolean reversing;

    public SubListChangeMove(ListVariableDescriptor<Solution_> variableDescriptor, SubList subList, Object destinationEntity,
            int destinationIndex, boolean reversing) {
        this(variableDescriptor, subList.getEntity(), subList.getFromIndex(), subList.getLength(), destinationEntity,
                destinationIndex, reversing);
    }

    public SubListChangeMove(
            ListVariableDescriptor<Solution_> variableDescriptor,
            Object sourceEntity, int sourceIndex, int length,
            Object destinationEntity, int destinationIndex, boolean reversing) {
        this.variableDescriptor = variableDescriptor;
        this.sourceEntity = sourceEntity;
        this.sourceIndex = sourceIndex;
        this.length = length;
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
        this.reversing = reversing;
    }

    public Object getSourceEntity() {
        return sourceEntity;
    }

    public int getFromIndex() {
        return sourceIndex;
    }

    public int getSubListSize() {
        return length;
    }

    public int getToIndex() {
        return sourceIndex + length;
    }

    public boolean isReversing() {
        return reversing;
    }

    public Object getDestinationEntity() {
        return destinationEntity;
    }

    public int getDestinationIndex() {
        return destinationIndex;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new SubListChangeMove<>(variableDescriptor, destinationEntity, destinationIndex, length, sourceEntity,
                sourceIndex, reversing);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;

        List<Object> sourceList = variableDescriptor.getListVariable(sourceEntity);
        List<Object> subList = sourceList.subList(sourceIndex, sourceIndex + length);
        List<Object> subListCopy = new ArrayList<>(subList);
        if (reversing) {
            Collections.reverse(subListCopy);
        }

        if (sourceEntity == destinationEntity) {
            int fromIndex = Math.min(sourceIndex, destinationIndex);
            int toIndex = Math.max(sourceIndex, destinationIndex) + length;
            innerScoreDirector.beforeSubListChanged(variableDescriptor, sourceEntity, fromIndex, toIndex);
            subList.clear();
            variableDescriptor.getListVariable(destinationEntity).addAll(destinationIndex, subListCopy);
            innerScoreDirector.afterSubListChanged(variableDescriptor, sourceEntity, fromIndex, toIndex);
        } else {
            innerScoreDirector.beforeSubListChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex + length);
            subList.clear();
            innerScoreDirector.afterSubListChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex);
            innerScoreDirector.beforeSubListChanged(variableDescriptor, destinationEntity, destinationIndex, destinationIndex);
            variableDescriptor.getListVariable(destinationEntity).addAll(destinationIndex, subListCopy);
            innerScoreDirector.afterSubListChanged(variableDescriptor, destinationEntity, destinationIndex,
                    destinationIndex + length);
        }
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return destinationEntity != sourceEntity
                || destinationIndex + length <= variableDescriptor.getListSize(destinationEntity)
                        && destinationIndex != sourceIndex;
    }

    @Override
    public String toString() {
        return String.format("|%d| {%s[%d..%d] -%s> %s[%d]}",
                length, sourceEntity, sourceIndex, getToIndex(),
                reversing ? "reversing-" : "", destinationEntity, destinationIndex);
    }
}
