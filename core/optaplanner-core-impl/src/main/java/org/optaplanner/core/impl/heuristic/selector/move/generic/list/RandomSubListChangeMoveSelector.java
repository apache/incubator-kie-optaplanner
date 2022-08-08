package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class RandomSubListChangeMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final EntitySelector<Solution_> entitySelector;
    private final EntityIndependentValueSelector<Solution_> valueSelector;
    private final int minimumSubListSize;
    private final int maximumSubListSize;

    private SingletonInverseVariableSupply inverseVariableSupply;

    public RandomSubListChangeMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntitySelector<Solution_> entitySelector,
            EntityIndependentValueSelector<Solution_> valueSelector,
            int minimumSubListSize, int maximumSubListSize) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        this.minimumSubListSize = minimumSubListSize;
        this.maximumSubListSize = maximumSubListSize;
        phaseLifecycleSupport.addEventListener(entitySelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
        inverseVariableSupply = supplyManager.demand(new SingletonListInverseVariableDemand<>(listVariableDescriptor));
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        inverseVariableSupply = null;
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new RandomSubListChangeMoveIterator<>(listVariableDescriptor, inverseVariableSupply, valueSelector,
                entitySelector, minimumSubListSize, maximumSubListSize, workingRandom);
    }

    @Override
    public boolean isCountable() {
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        return true;
    }

    @Override
    public long getSize() {
        long d = entitySelector.getSize() + valueSelector.getSize();
        long moveCount = 0;
        for (Object entity : (Iterable<?>) entitySelector::endingIterator) {
            int n = listVariableDescriptor.getListSize(entity);
            int triangle = TriangularNumbers.nth(n);
            int pyramid = triangle * (2 * n + 1) / 3;
            moveCount += triangle * (d - n - 2) + pyramid;
        }
        return moveCount;
    }
}
