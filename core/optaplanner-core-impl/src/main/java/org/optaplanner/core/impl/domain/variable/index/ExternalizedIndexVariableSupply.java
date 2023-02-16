package org.optaplanner.core.impl.domain.variable.index;

import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.SourcedVariableListener;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

/**
 * Alternative to {@link IndexVariableListener}.
 */
public class ExternalizedIndexVariableSupply<Solution_> implements
        SourcedVariableListener<Solution_>,
        ListVariableListener<Solution_, Object, Object>,
        IndexVariableSupply {

    private static final int UNASSIGNED_VALUE = -1;

    protected final ListVariableDescriptor<Solution_> sourceVariableDescriptor;

    // Specialized collection as Integer boxing overhead on the hot path was measured to be significant.
    protected Object2IntMap<Object> indexMap = null;

    public ExternalizedIndexVariableSupply(ListVariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public VariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(ScoreDirector<Solution_> scoreDirector) {
        indexMap = new Object2IntOpenCustomHashMap<>(new IdentityHashStrategy<>());
        indexMap.defaultReturnValue(UNASSIGNED_VALUE);
        sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(scoreDirector.getWorkingSolution(), this::insert);
    }

    @Override
    public void close() {
        indexMap = null;
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<Solution_> scoreDirector, Object entity) {
        insert(entity);
    }

    @Override
    public void afterListVariableElementUnassigned(ScoreDirector<Solution_> scoreDirector, Object element) {
        int oldIndex = indexMap.removeInt(element);
        if (oldIndex == UNASSIGNED_VALUE) {
            throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                    + " because the element (" + element
                    + ") has an oldIndex (" + oldIndex
                    + ") which represents null.");
        }
    }

    @Override
    public void beforeListVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, int fromIndex, int toIndex) {
        // Do nothing
    }

    @Override
    public void afterListVariableChanged(ScoreDirector<Solution_> scoreDirector, Object entity, int fromIndex, int toIndex) {
        updateIndexes(entity, fromIndex);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // When the entity is removed, its values become unassigned. An unassigned value has no inverse entity and no index.
        retract(entity);
    }

    private void insert(Object entity) {
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        int index = 0;
        for (Object element : listVariable) {
            int oldIndex = indexMap.put(element, index);
            if (oldIndex != UNASSIGNED_VALUE) {
                throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                        + " because the element (" + element
                        + ") at index (" + index
                        + ") has an oldIndex (" + oldIndex
                        + ") which does not represent null.");
            }
            index++;
        }
    }

    private void retract(Object entity) {
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        int index = 0;
        for (Object element : listVariable) {
            int oldIndex = indexMap.removeInt(element);
            if (!Objects.equals(oldIndex, index)) {
                throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                        + " because the element (" + element
                        + ") at index (" + index
                        + ") has an oldIndex (" + oldIndex
                        + ") which is unexpected.");
            }
            index++;
        }
    }

    private void updateIndexes(Object entity, int startIndex) {
        List<Object> listVariable = sourceVariableDescriptor.getListVariable(entity);
        int listVariableSize = listVariable.size();
        for (int index = startIndex; index < listVariableSize; index++) {
            Object element = listVariable.get(index);
            int oldIndex = indexMap.put(element, index);
            // The first element is allowed to have a null oldIndex because it might have been just assigned.
            if (oldIndex == UNASSIGNED_VALUE && index != startIndex) {
                throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                        + " because the element (" + element
                        + ") at index (" + index
                        + ") has an oldIndex (" + oldIndex
                        + ") which represents null.");
            }
        }
    }

    @Override
    public int getIndex(Object element) {
        return indexMap.getInt(element);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceVariableDescriptor.getVariableName() + ")";
    }

    /**
     * Treats two objects as equal iff they are, in fact, the same.
     *
     * @param <T>
     */
    private static final class IdentityHashStrategy<T> implements Hash.Strategy<T> {

        @Override
        public int hashCode(T o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(T a, T b) {
            return a == b;
        }
    }
}
