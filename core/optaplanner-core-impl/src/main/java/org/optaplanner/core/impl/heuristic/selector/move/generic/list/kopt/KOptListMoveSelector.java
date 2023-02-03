package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class KOptListMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final EntitySelector<Solution_> entitySelector;
    private final ValueSelector<Solution_> valueSelector;
    private final int minK;
    private final int maxK;
    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    public KOptListMoveSelector(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            EntitySelector<Solution_> entitySelector,
            ValueSelector<Solution_> valueSelector,
            int minK,
            int maxK) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entitySelector = entitySelector;
        this.valueSelector = valueSelector;
        this.minK = minK;
        this.maxK = maxK;
        phaseLifecycleSupport.addEventListener(entitySelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
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
    public long getSize() {
        Iterator<Object> entityIterator = entitySelector.endingIterator();
        long total = 0;
        while (entityIterator.hasNext()) {
            Object entity = entityIterator.next();
            long valueSelectorSize = valueSelector.getSize(entity);

            for (int i = minK; i < Math.min(valueSelectorSize, maxK); i++) {
                total += factorialUpToCount(valueSelectorSize, i);
            }
        }
        return total;
    }

    private long factorialUpToCount(long toMultiply, int count) {
        long total = toMultiply;
        for (int i = 1; i < count; i++) {
            total *= (toMultiply - i);
        }
        return total;
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new KOptListMoveIterator(workingRandom, listVariableDescriptor, inverseVariableSupply, indexVariableSupply,
                entitySelector, valueSelector, minK, maxK);
    }

    @Override
    public boolean isCountable() {
        return false;
    }

    @Override
    public boolean isNeverEnding() {
        return true;
    }
}
