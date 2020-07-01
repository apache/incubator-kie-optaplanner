/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChain;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SubChainReversingChangeMove<Solution_> extends AbstractMove<Solution_> {

    protected final SubChain subChain;
    protected final GenuineVariableDescriptor<Solution_> variableDescriptor;
    protected final Object toPlanningValue;

    protected final Object oldTrailingLastEntity;
    protected final Object newTrailingEntity;

    public SubChainReversingChangeMove(SubChain subChain, GenuineVariableDescriptor<Solution_> variableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply, Object toPlanningValue) {
        this.subChain = subChain;
        this.variableDescriptor = variableDescriptor;
        this.toPlanningValue = toPlanningValue;
        oldTrailingLastEntity = inverseVariableSupply.getInverseSingleton(subChain.getLastEntity());
        newTrailingEntity = toPlanningValue == null ? null
                : inverseVariableSupply.getInverseSingleton(toPlanningValue);
    }

    public SubChainReversingChangeMove(SubChain subChain, GenuineVariableDescriptor<Solution_> variableDescriptor,
            Object toPlanningValue, Object oldTrailingLastEntity, Object newTrailingEntity) {
        this.subChain = subChain;
        this.variableDescriptor = variableDescriptor;
        this.toPlanningValue = toPlanningValue;
        this.oldTrailingLastEntity = oldTrailingLastEntity;
        this.newTrailingEntity = newTrailingEntity;
    }

    public String getVariableName() {
        return variableDescriptor.getVariableName();
    }

    public SubChain getSubChain() {
        return subChain;
    }

    public Object getToPlanningValue() {
        return toPlanningValue;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        if (subChain.getEntityList().contains(toPlanningValue)) {
            return false;
        }
        Object oldFirstValue = variableDescriptor.getValue(subChain.getFirstEntity());
        return !Objects.equals(oldFirstValue, toPlanningValue);
    }

    @Override
    public SubChainReversingChangeMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        Object oldFirstValue = variableDescriptor.getValue(subChain.getFirstEntity());
        boolean unmovedReverse = toPlanningValue == oldFirstValue;
        if (!unmovedReverse) {
            return new SubChainReversingChangeMove<>(subChain.reverse(), variableDescriptor, oldFirstValue,
                    newTrailingEntity, oldTrailingLastEntity);
        } else {
            return new SubChainReversingChangeMove<>(subChain.reverse(), variableDescriptor, oldFirstValue,
                    oldTrailingLastEntity, newTrailingEntity);
        }
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        Object firstEntity = subChain.getFirstEntity();
        Object lastEntity = subChain.getLastEntity();
        Object oldFirstValue = variableDescriptor.getValue(firstEntity);
        boolean unmovedReverse = toPlanningValue == oldFirstValue;
        // Close the old chain
        InnerScoreDirector<Solution_> innerScoreDirector = (InnerScoreDirector<Solution_>) scoreDirector;
        if (!unmovedReverse) {
            if (oldTrailingLastEntity != null) {
                innerScoreDirector.changeVariableFacade(variableDescriptor, oldTrailingLastEntity, oldFirstValue);
            }
        }
        Object lastEntityValue = variableDescriptor.getValue(lastEntity);
        // Change the entity
        innerScoreDirector.changeVariableFacade(variableDescriptor, lastEntity, toPlanningValue);
        // Reverse the chain
        reverseChain(innerScoreDirector, lastEntity, lastEntityValue, firstEntity);
        // Reroute the new chain
        if (!unmovedReverse) {
            if (newTrailingEntity != null) {
                innerScoreDirector.changeVariableFacade(variableDescriptor, newTrailingEntity, firstEntity);
            }
        } else {
            if (oldTrailingLastEntity != null) {
                innerScoreDirector.changeVariableFacade(variableDescriptor, oldTrailingLastEntity, firstEntity);
            }
        }
    }

    private void reverseChain(InnerScoreDirector<Solution_> scoreDirector, Object entity, Object previous,
            Object toEntity) {
        while (entity != toEntity) {
            Object value = variableDescriptor.getValue(previous);
            scoreDirector.changeVariableFacade(variableDescriptor, previous, entity);
            entity = previous;
            previous = value;
        }
    }

    @Override
    public SubChainReversingChangeMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new SubChainReversingChangeMove<>(subChain.rebase(destinationScoreDirector),
                variableDescriptor,
                destinationScoreDirector.lookUpWorkingObject(toPlanningValue),
                destinationScoreDirector.lookUpWorkingObject(oldTrailingLastEntity),
                destinationScoreDirector.lookUpWorkingObject(newTrailingEntity));
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return subChain.getEntityList();
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toPlanningValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SubChainReversingChangeMove<?> other = (SubChainReversingChangeMove<?>) o;
        return Objects.equals(subChain, other.subChain) &&
                Objects.equals(variableDescriptor.getVariableName(), other.variableDescriptor.getVariableName()) &&
                Objects.equals(toPlanningValue, other.toPlanningValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subChain, variableDescriptor.getVariableName(), toPlanningValue);
    }

    @Override
    public String toString() {
        Object oldFirstValue = variableDescriptor.getValue(subChain.getFirstEntity());
        return subChain.toDottedString() + " {" + oldFirstValue + " -reversing-> " + toPlanningValue + "}";
    }

}
