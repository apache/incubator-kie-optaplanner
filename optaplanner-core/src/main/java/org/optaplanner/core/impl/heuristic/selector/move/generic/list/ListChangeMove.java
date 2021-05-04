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

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

/**
 * Moves an element of a list planning variable. The moved element is identified by an entity instance and a position
 * in that entity's list variable. The element is inserted at the given index in the given destination entity's list
 * variable.
 * <p>
 * The move can be undone by simply flipping the source and destination entity+index
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class ListChangeMove<Solution_> extends AbstractMove<Solution_> {

    private final Object sourceEntity;
    private final int sourceIndex;
    private final Object destinationEntity;
    private final int destinationIndex;

    private final DefaultListVariableDescriptor<Solution_> variableDescriptor; // Employee::getTaskList()

    public ListChangeMove(
            Object sourceEntity, int sourceIndex,
            Object destinationEntity, int destinationIndex,
            DefaultListVariableDescriptor<Solution_> variableDescriptor) {
        this.sourceEntity = sourceEntity; //= anchorSupply.get(entityElement)
        this.sourceIndex = sourceIndex; //= indexSupply.get(entityElement)
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
        this.variableDescriptor = variableDescriptor;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new ListChangeMove<>(destinationEntity, destinationIndex, sourceEntity, sourceIndex, variableDescriptor);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        Object element = variableDescriptor.removeElement(sourceEntity, sourceIndex);
        variableDescriptor.addElement(destinationEntity, destinationIndex, element);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // TODO maybe remove this because no such move should be generated
        return !destinationEntity.equals(sourceEntity)
                || (destinationIndex != sourceIndex && destinationIndex != variableDescriptor.getListSize(sourceEntity));
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
}
