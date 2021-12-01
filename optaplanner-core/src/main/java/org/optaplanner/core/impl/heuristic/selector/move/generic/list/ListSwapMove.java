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
import org.optaplanner.core.api.domain.variable.PlanningCollectionVariable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * Swaps two elements of a {@link PlanningCollectionVariable list variable}.
 * Each element is identified by an entity instance and an index in that entity's list variable.
 * The swap move has two sides called left and right. The element at {@code leftIndex} in {@code leftEntity}'s list variable
 * is replaced by the element at {@code rightIndex} in {@code rightEntity}'s list variable and vice versa.
 * Left and right entity can be the same instance.
 * <p>
 * An undo move is created by flipping the left and right-hand entity and index.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class ListSwapMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final Object leftEntity;
    private final int leftIndex;
    private final Object rightEntity;
    private final int rightIndex;

    /**
     * Create a move that swaps a list variable element at {@code leftEntity.listVariable[leftIndex]} with
     * {@code rightEntity.listVariable[rightIndex]}.
     *
     * <h4>ListSwapMove anatomy</h4>
     *
     * <pre>
     * {@code
     *                                 ┌ rightEntity
     *                right element ┐  │   ┌ rightIndex
     *                              ↓  ↓   ↓
     *               A {Ann[0]} <-> Y {Bob[1]}
     *               ↑  ↑   ↑
     *  left element ┘  │   └ leftIndex
     *                  └ leftEntity
     * }
     * </pre>
     *
     * <h4>Example</h4>
     *
     * <pre>
     * {@code
     * GIVEN
     * Ann.tasks = [A, B, C]
     * Bob.tasks = [X, Y]
     *
     * WHEN
     * ListSwapMove: A {Ann[0]} <-> Y {Bob[1]}
     *
     * THEN
     * Ann.tasks = [Y, B, C]
     * Bob.tasks = [X, A]
     * }
     * </pre>
     *
     * @param variableDescriptor descriptor of a list variable, for example {@code Employee.taskList}
     * @param leftEntity together with {@code leftIndex} identifies the left element to be moved
     * @param leftIndex together with {@code leftEntity} identifies the left element to be moved
     * @param rightEntity together with {@code rightIndex} identifies the right element to be moved
     * @param rightIndex together with {@code rightEntity} identifies the right element to be moved
     */
    public ListSwapMove(
            ListVariableDescriptor<Solution_> variableDescriptor,
            Object leftEntity, int leftIndex,
            Object rightEntity, int rightIndex) {
        this.variableDescriptor = variableDescriptor;
        this.leftEntity = leftEntity;
        this.leftIndex = leftIndex;
        this.rightEntity = rightEntity;
        this.rightIndex = rightIndex;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new ListSwapMove<>(variableDescriptor, rightEntity, rightIndex, leftEntity, leftIndex);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        Object leftElement = variableDescriptor.getElement(leftEntity, leftIndex);
        Object rightElement = variableDescriptor.getElement(rightEntity, rightIndex);

        innerScoreDirector.beforeVariableChanged(variableDescriptor, leftEntity, leftIndex);
        variableDescriptor.setElement(leftEntity, leftIndex, rightElement);
        innerScoreDirector.afterVariableChanged(variableDescriptor, leftEntity, leftIndex);

        innerScoreDirector.beforeVariableChanged(variableDescriptor, rightEntity, rightIndex);
        variableDescriptor.setElement(rightEntity, rightIndex, leftElement);
        innerScoreDirector.afterVariableChanged(variableDescriptor, rightEntity, rightIndex);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // TODO maybe do not generate such moves
        // Do not use Object#equals on user-provided domain objects. Relying on user's implementation of Object#equals
        // opens the opportunity to shoot themselves in the foot if different entities can be equal.
        return !(rightEntity == leftEntity && leftIndex == rightIndex);
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    // ************************************************************************
    // Testing methods
    // ************************************************************************

    public Object getLeftEntity() {
        return leftEntity;
    }

    public int getLeftIndex() {
        return leftIndex;
    }

    public Object getRightEntity() {
        return rightEntity;
    }

    public int getRightIndex() {
        return rightIndex;
    }

    public Object getLeftValue() {
        return variableDescriptor.getElement(leftEntity, leftIndex);
    }

    public Object getRightValue() {
        return variableDescriptor.getElement(rightEntity, rightIndex);
    }

    @Override
    public String toString() {
        return String.format("%s {%s[%d]} <-> %s {%s[%d]}",
                getLeftValue(), leftEntity, leftIndex,
                getRightValue(), rightEntity, rightIndex);
    }
}
