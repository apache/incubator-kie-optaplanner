package org.optaplanner.core.impl.domain.variable.nextprev;

import java.util.List;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class NextElementVariableListener<Solution_> implements ListVariableListener<Solution_, Object> {

    protected final NextElementShadowVariableDescriptor<Solution_> shadowVariableDescriptor;
    protected final ListVariableDescriptor<Solution_> sourceVariableDescriptor;

    public NextElementVariableListener(
            NextElementShadowVariableDescriptor<Solution_> shadowVariableDescriptor,
            ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.shadowVariableDescriptor = shadowVariableDescriptor;
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        for (int i = 0; i < listVariable.size() - 1; i++) {
            Object element = listVariable.get(i);
            Object next = listVariable.get(i + 1);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, next);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        for (int i = 0; i < listVariable.size() - 1; i++) {
            Object element = listVariable.get(i);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public void beforeElementAdded(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        // Do nothing
    }

    @Override
    public void afterElementAdded(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        Object element = listVariable.get(index);
        if (index < listVariable.size() - 1) {
            // TODO maybe if next != element.next
            Object next = listVariable.get(index + 1);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, next);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        } else if (shadowVariableDescriptor.getValue(element) != null) {
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
        if (index > 0) {
            Object previous = listVariable.get(index - 1);
            if (element != shadowVariableDescriptor.getValue(previous)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, previous);
                shadowVariableDescriptor.setValue(previous, element);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, previous);
            }
        }
    }

    @Override
    public void beforeElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        Object element = listVariable.get(index);
        if (index < listVariable.size() - 1) { // The last element already has its next==null, so we can skip it.
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public void afterElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        if (index > 0) {
            Object previous = listVariable.get(index - 1);
            Object newNext = index == listVariable.size() ? null : listVariable.get(index);
            if (newNext != shadowVariableDescriptor.getValue(previous)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, previous);
                shadowVariableDescriptor.setValue(previous, newNext);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, previous);
            }
        }
    }

    @Override
    public void beforeElementMoved(ScoreDirector<Solution_> scoreDirector,
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex) {
        // Do nothing
    }

    @Override
    public void afterElementMoved(ScoreDirector<Solution_> scoreDirector,
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex) {
        if (sourceEntity != destinationEntity) {
            afterElementRemoved(scoreDirector, sourceEntity, sourceIndex);
            afterElementAdded(scoreDirector, destinationEntity, destinationIndex);
        } else {
            afterElementRemoved(scoreDirector, sourceEntity, sourceIndex > destinationIndex ? sourceIndex + 1 : sourceIndex);
            afterElementAdded(scoreDirector, destinationEntity, destinationIndex);
        }
    }

    @Override
    public void beforeSubListChanged(ScoreDirector<Solution_> scoreDirector, Object entity, int fromIndex, int toIndex) {
        // Do nothing
    }

    @Override
    public void afterSubListChanged(ScoreDirector<Solution_> scoreDirector, Object entity, int fromIndex, int toIndex) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        Object next = toIndex < listVariable.size() ? listVariable.get(toIndex) : null;
        for (int i = toIndex - 1; i >= fromIndex - 1 && i >= 0; i--) {
            Object element = listVariable.get(i);
            if (next != shadowVariableDescriptor.getValue(element)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
                shadowVariableDescriptor.setValue(element, next);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
            }
            next = element;
        }
    }
}
