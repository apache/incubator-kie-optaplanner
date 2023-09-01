/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import java.util.Arrays;
import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.anchor.AnchorVariableDemand;
import org.optaplanner.core.impl.domain.variable.anchor.AnchorVariableSupply;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class KOptMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    protected final EntitySelector<Solution_> entitySelector;
    protected final ValueSelector<Solution_>[] valueSelectors;
    protected final boolean randomSelection;
    protected final GenuineVariableDescriptor<Solution_> variableDescriptor;

    protected SingletonInverseVariableSupply inverseVariableSupply;
    protected AnchorVariableSupply anchorVariableSupply;

    public KOptMoveSelector(EntitySelector<Solution_> entitySelector, ValueSelector<Solution_>[] valueSelectors,
            boolean randomSelection) {
        this.entitySelector = entitySelector;
        this.valueSelectors = valueSelectors;
        this.randomSelection = randomSelection;
        if (!randomSelection) {
            throw new UnsupportedOperationException(
                    "Non randomSelection (such as original selection) is not yet supported on "
                            + KOptMoveSelector.class.getSimpleName() + "."); // TODO
        }
        variableDescriptor = valueSelectors[0].getVariableDescriptor();
        if (!variableDescriptor.isChained()) {
            throw new IllegalStateException("The selector (" + this
                    + ")'s valueSelector's  variableDescriptor (" + variableDescriptor
                    + ") must be chained (" + variableDescriptor.isChained() + ").");
        }
        if (!variableDescriptor.getEntityDescriptor().getEntityClass().isAssignableFrom(
                entitySelector.getEntityDescriptor().getEntityClass())) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a valueSelector with a entityClass ("
                    + variableDescriptor.getEntityDescriptor().getEntityClass()
                    + ") which is not equal or a superclass to the entitySelector's entityClass ("
                    + entitySelector.getEntityDescriptor().getEntityClass() + ").");
        }
        phaseLifecycleSupport.addEventListener(entitySelector);
        for (ValueSelector<Solution_> valueSelector : valueSelectors) {
            if (valueSelector.getVariableDescriptor() != variableDescriptor) {
                throw new IllegalStateException("The selector (" + this
                        + ") has a valueSelector with a variableDescriptor (" + valueSelector.getVariableDescriptor()
                        + ") that differs from the first variableDescriptor (" + variableDescriptor + ").");
            }
            phaseLifecycleSupport.addEventListener(valueSelector);
        }
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        inverseVariableSupply = supplyManager.demand(new SingletonInverseVariableDemand<>(variableDescriptor));
        anchorVariableSupply = supplyManager.demand(new AnchorVariableDemand<>(variableDescriptor));
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        inverseVariableSupply = null;
        anchorVariableSupply = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        if (!entitySelector.isCountable()) {
            return false;
        }
        for (ValueSelector<Solution_> valueSelector : valueSelectors) {
            if (!valueSelector.isCountable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        if (randomSelection || entitySelector.isNeverEnding()) {
            return true;
        }
        for (ValueSelector<Solution_> valueSelector : valueSelectors) {
            if (valueSelector.isNeverEnding()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Not yet supported."); // TODO
        //        if (valueSelector instanceof IterableSelector) {
        //            return entitySelector.getSize() * (long) Math.pow(((IterableSelector) valueSelector).getSize(), K);
        //        } else {
        //        }
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        if (!randomSelection) {
            throw new UnsupportedOperationException(
                    "Non randomSelection (such as original selection) is not yet supported on "
                            + KOptMoveSelector.class.getSimpleName() + "."); // TODO
        } else {
            final Iterator<Object> entityIterator = entitySelector.iterator();
            return new UpcomingSelectionIterator<>() {
                @Override
                protected Move<Solution_> createUpcomingSelection() {
                    // TODO currently presumes that entitySelector and all valueSelectors are never ending, despite the hasNext() checks
                    if (!entityIterator.hasNext()) {
                        return noUpcomingSelection();
                    }
                    Object entity = entityIterator.next();
                    Object[] values = new Object[valueSelectors.length];
                    for (int i = 0; i < valueSelectors.length; i++) {
                        Iterator<Object> valueIterator = valueSelectors[i].iterator(entity);
                        if (!valueIterator.hasNext()) {
                            return noUpcomingSelection();
                        }
                        values[i] = valueIterator.next();
                    }
                    return new KOptMove<>(variableDescriptor, inverseVariableSupply, anchorVariableSupply, entity,
                            values);
                }
            };
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entitySelector + ", " + Arrays.toString(valueSelectors) + ")";
    }

}
