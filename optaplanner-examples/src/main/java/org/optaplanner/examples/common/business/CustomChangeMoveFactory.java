package org.optaplanner.examples.common.business;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.entity.AbstractEntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;

final class CustomChangeMoveFactory<Solution_> {

    private final EntityDescriptor<Solution_> entityDescriptor;
    private final GenuineVariableDescriptor<Solution_> variableDescriptor;

    public CustomChangeMoveFactory(EntityDescriptor<Solution_> entityDescriptor,
            GenuineVariableDescriptor<Solution_> variableDescriptor) {
        this.entityDescriptor = entityDescriptor;
        this.variableDescriptor = variableDescriptor;
    }

    public ChangeMove<Solution_> create(Object entity, Object toPlanningValue) {
        ChangeMoveSelector<Solution_> changeMoveSelector = new ChangeMoveSelector<>(
                new SingleEntitySelector<>(entityDescriptor, entity),
                new SingleValueSelector<>(variableDescriptor, toPlanningValue), false);
        return (ChangeMove<Solution_>) changeMoveSelector.iterator().next();
    }

    private static final class SingleEntitySelector<Solution_> extends AbstractEntitySelector<Solution_> {

        private final EntityDescriptor<Solution_> entityDescriptor;
        private final List<Object> objectList;

        public SingleEntitySelector(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
            this.entityDescriptor = entityDescriptor;
            this.objectList = Collections.singletonList(entity);
        }

        @Override
        public long getSize() {
            return 1;
        }

        @Override
        public boolean isCountable() {
            return true;
        }

        @Override
        public boolean isNeverEnding() {
            return false;
        }

        @Override
        public ListIterator<Object> listIterator() {
            return objectList.listIterator();
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            return objectList.listIterator(index);
        }

        @Override
        public EntityDescriptor<Solution_> getEntityDescriptor() {
            return entityDescriptor;
        }

        @Override
        public Iterator<Object> endingIterator() {
            return iterator();
        }

        @Override
        public Iterator<Object> iterator() {
            return objectList.iterator();
        }
    }

    private static final class SingleValueSelector<Solution_> extends AbstractValueSelector<Solution_> {

        private final GenuineVariableDescriptor<Solution_> variableDescriptor;
        private final List<Object> objectList;

        public SingleValueSelector(GenuineVariableDescriptor<Solution_> variableDescriptor, Object value) {
            this.variableDescriptor = variableDescriptor;
            this.objectList = Collections.singletonList(value);
        }

        @Override
        public boolean isCountable() {
            return true;
        }

        @Override
        public boolean isNeverEnding() {
            return false;
        }

        @Override
        public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
            return variableDescriptor;
        }

        @Override
        public long getSize(Object entity) {
            return 1;
        }

        @Override
        public Iterator<Object> iterator(Object entity) {
            return objectList.iterator();
        }

        @Override
        public Iterator<Object> endingIterator(Object entity) {
            return iterator(entity);
        }
    }
}
