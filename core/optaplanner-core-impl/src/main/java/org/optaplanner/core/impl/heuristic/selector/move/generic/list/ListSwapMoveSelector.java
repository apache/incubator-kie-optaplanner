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

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class ListSwapMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final EntityIndependentValueSelector<Solution_> leftValueSelector;
    private final EntityIndependentValueSelector<Solution_> rightValueSelector;
    private final boolean randomSelection;

    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    public ListSwapMoveSelector(
            EntityIndependentValueSelector<Solution_> leftValueSelector,
            EntityIndependentValueSelector<Solution_> rightValueSelector,
            boolean randomSelection) {
        // TODO require not same
        this.leftValueSelector = leftValueSelector;
        this.rightValueSelector = rightValueSelector;
        this.randomSelection = randomSelection;
        phaseLifecycleSupport.addEventListener(leftValueSelector);
        if (leftValueSelector != rightValueSelector) {
            phaseLifecycleSupport.addEventListener(rightValueSelector);
        }
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        ListVariableDescriptor<Solution_> listVariableDescriptor =
                (ListVariableDescriptor<Solution_>) leftValueSelector.getVariableDescriptor();
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        inverseVariableSupply = supplyManager.demand(new SingletonListInverseVariableDemand<>(listVariableDescriptor));
        indexVariableSupply = supplyManager.demand(new IndexVariableDemand<>(listVariableDescriptor));
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        inverseVariableSupply = null;
        indexVariableSupply = null;
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        if (randomSelection) {
            return new RandomListSwapIterator<>(
                    inverseVariableSupply,
                    indexVariableSupply,
                    leftValueSelector,
                    rightValueSelector);
        } else {
            return new OriginalListSwapIterator<>(
                    inverseVariableSupply,
                    indexVariableSupply,
                    leftValueSelector,
                    rightValueSelector);
        }
    }

    @Override
    public boolean isCountable() {
        return leftValueSelector.isCountable() && rightValueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || leftValueSelector.isNeverEnding() || rightValueSelector.isNeverEnding();
    }

    @Override
    public long getSize() {
        return leftValueSelector.getSize() * rightValueSelector.getSize();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + leftValueSelector + ", " + rightValueSelector + ")";
    }
}
