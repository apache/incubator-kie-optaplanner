package org.optaplanner.core.impl.domain.variable.previous;

import java.util.List;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class PreviousElementVariableListener<Solution_> implements ListVariableListener<Solution_, Object> {

    protected final PreviousElementShadowVariableDescriptor<Solution_> shadowVariableDescriptor;
    protected final ListVariableDescriptor<Solution_> sourceVariableDescriptor;

    public PreviousElementVariableListener(
            PreviousElementShadowVariableDescriptor<Solution_> shadowVariableDescriptor,
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
        for (int i = 1; i < listVariable.size(); i++) {
            Object element = listVariable.get(i);
            Object previous = listVariable.get(i - 1);
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, previous);
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
        for (int i = 1; i < listVariable.size(); i++) {
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
        if (index > 0) {
            Object previous = listVariable.get(index - 1);
            if (previous != shadowVariableDescriptor.getValue(element)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
                shadowVariableDescriptor.setValue(element, previous);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
            }
        } else if (shadowVariableDescriptor.getValue(element) != null) {
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
        if (index < listVariable.size() - 1) {
            Object next = listVariable.get(index + 1);
            if (element != shadowVariableDescriptor.getValue(next)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, next);
                shadowVariableDescriptor.setValue(next, element);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, next);
            }
        }
    }

    @Override
    public void beforeElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        Object element = listVariable.get(index);
        if (index > 0) { // The first element already has its previous==null, so we can skip it.
            innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
            shadowVariableDescriptor.setValue(element, null);
            innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
        }
    }

    @Override
    public void afterElementRemoved(ScoreDirector<Solution_> scoreDirector, Object entity, int index) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        if (index < listVariable.size()) {
            Object element = listVariable.get(index);
            Object newPrevious = index == 0 ? null : listVariable.get(index - 1);
            if (newPrevious != shadowVariableDescriptor.getValue(element)) {
                innerScoreDirector.beforeVariableChanged(shadowVariableDescriptor, element);
                shadowVariableDescriptor.setValue(element, newPrevious);
                innerScoreDirector.afterVariableChanged(shadowVariableDescriptor, element);
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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void afterSubListChanged(ScoreDirector<Solution_> scoreDirector, Object entity, int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("TODO");
    }
}
