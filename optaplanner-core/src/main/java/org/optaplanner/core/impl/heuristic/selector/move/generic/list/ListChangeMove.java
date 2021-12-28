/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.PlanningCollectionVariable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * Moves an element of a {@link PlanningCollectionVariable list variable}. The moved element is identified
 * by an entity instance and a position in that entity's list variable. The element is inserted at the given index
 * in the given destination entity's list variable.
 * <p>
 * An undo move is simply created by flipping the source and destination entity+index.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class ListChangeMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final Object sourceEntity;
    private final int sourceIndex;
    private final Object destinationEntity;
    private final int destinationIndex;

    /**
     * The move removes a planning value element from {@code sourceEntity.listVariable[sourceIndex]}
     * and inserts the planning value at {@code destinationEntity.listVariable[destinationIndex]}.
     *
     * <h4>ListChangeMove anatomy</h4>
     *
     * <pre>
     * {@code
     *                             ┌ destinationEntity
     *                             │   ┌ destinationIndex
     *                             ↓   ↓
     *                A {Ann[0]}->{Bob[2]}
     *                ↑  ↑   ↑
     * planning value ┘  │   └ sourceIndex
     *                   └ sourceEntity
     * }
     * </pre>
     *
     * <h4>Example 1 - source and destination entities are different</h4>
     *
     * <pre>
     * {@code
     * GIVEN
     * Ann.tasks = [A, B, C]
     * Bob.tasks = [X, Y]
     *
     * WHEN
     * ListChangeMove: A {Ann[0]->Bob[2]}
     *
     * THEN
     * Ann.tasks = [B, C]
     * Bob.tasks = [X, Y, A]
     * }
     * </pre>
     *
     * <h4>Example 2 - source and destination is the same entity</h4>
     *
     * <pre>
     * {@code
     * GIVEN
     * Ann.tasks = [A, B, C]
     *
     * WHEN
     * ListChangeMove: A {Ann[0]->Ann[2]}
     *
     * THEN
     * Ann.tasks = [B, C, A]
     * }
     * </pre>
     *
     * @param variableDescriptor descriptor of a list variable, for example {@code Employee.taskList}
     * @param sourceEntity planning entity instance from which a planning value will be removed, for example "Ann"
     * @param sourceIndex index in sourceEntity's list variable from which a planning value will be removed
     * @param destinationEntity planning entity instance to which a planning value will be moved, for example "Bob"
     * @param destinationIndex index in destinationEntity's list variable where the moved planning value will be inserted
     */
    public ListChangeMove(
            ListVariableDescriptor<Solution_> variableDescriptor,
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex) {
        this.variableDescriptor = variableDescriptor;
        this.sourceEntity = sourceEntity;
        this.sourceIndex = sourceIndex;
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new ListChangeMove<>(variableDescriptor, destinationEntity, destinationIndex, sourceEntity, sourceIndex);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        // Remove element from the source entity.
        innerScoreDirector.beforeVariableChanged(variableDescriptor, sourceEntity);
        Object element = variableDescriptor.removeElement(sourceEntity, sourceIndex);
        innerScoreDirector.afterVariableChanged(variableDescriptor, sourceEntity);
        // Add element to the destination entity.
        innerScoreDirector.beforeVariableChanged(variableDescriptor, destinationEntity);
        variableDescriptor.addElement(destinationEntity, destinationIndex, element);
        innerScoreDirector.afterVariableChanged(variableDescriptor, destinationEntity);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // TODO maybe remove this because no such move should be generated
        // Do not use Object#equals on user-provided domain objects. Relying on user's implementation of Object#equals
        // opens the opportunity to shoot themselves in the foot if different entities can be equal.
        return destinationEntity != sourceEntity
                || (destinationIndex != sourceIndex && destinationIndex != variableDescriptor.getListSize(sourceEntity));
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<Object> getPlanningEntities() {
        // Use LinkedHashSet for predictable iteration order.
        Set<Object> entities = new LinkedHashSet<>(2);
        entities.add(sourceEntity);
        entities.add(destinationEntity);
        return entities;
    }

    @Override
    public Collection<Object> getPlanningValues() {
        return Collections.singleton(getMovedValue());
    }

    // ************************************************************************
    // Testing methods
    // ************************************************************************

    public Object getSourceEntity() {
        return sourceEntity;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public Object getDestinationEntity() {
        return destinationEntity;
    }

    public int getDestinationIndex() {
        return destinationIndex;
    }

    public Object getMovedValue() {
        return variableDescriptor.getElement(sourceEntity, sourceIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListChangeMove<?> other = (ListChangeMove<?>) o;
        return sourceIndex == other.sourceIndex && destinationIndex == other.destinationIndex
                && Objects.equals(variableDescriptor, other.variableDescriptor)
                && Objects.equals(sourceEntity, other.sourceEntity)
                && Objects.equals(destinationEntity, other.destinationEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableDescriptor, sourceEntity, sourceIndex, destinationEntity, destinationIndex);
    }

    @Override
    public String toString() {
        return String.format("%s {%s[%d] -> %s[%d]}",
                getMovedValue(), sourceEntity, sourceIndex, destinationEntity, destinationIndex);
    }
}
