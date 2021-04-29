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

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

public class ListChangeMove<Solution_> extends AbstractMove<Solution_> {

    private final Object entity;
    private final int index;
    private final Object toEntity;
    private final int toIndex;

    private final ListVariableMutator variableDescriptor; // Employee::getTaskList()

    public ListChangeMove(
            Object entity, int index,
            Object toEntity, int toIndex,
            ListVariableMutator variableDescriptor) {
        this.entity = entity; //= anchorSupply.get(entityElement)
        this.index = index; //= indexSupply.get(entityElement)
        this.toEntity = toEntity;
        this.toIndex = toIndex;
        this.variableDescriptor = variableDescriptor;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new ListChangeMove<>(toEntity, toIndex, entity, index, variableDescriptor);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        Object element = variableDescriptor.removeElement(entity, index);
        variableDescriptor.addElement(toEntity, toIndex, element);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // TODO maybe remove this because no such move should be generated
        return !entity.equals(toEntity) || index != toIndex;
    }
}
