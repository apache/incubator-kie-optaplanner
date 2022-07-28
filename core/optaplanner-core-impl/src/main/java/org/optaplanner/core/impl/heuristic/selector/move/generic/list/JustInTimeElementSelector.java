package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class JustInTimeElementSelector<Solution_> extends AbstractSelector<Solution_>
        implements Iterable<ElementRef> {

    private final EntityIndependentValueSelector<Solution_> valueSelector;
    private final ListVariableDescriptor<Solution_> listVariableDescriptor;

    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    public JustInTimeElementSelector(EntityIndependentValueSelector<Solution_> valueSelector) {
        this.valueSelector = valueSelector;
        listVariableDescriptor = (ListVariableDescriptor<Solution_>) valueSelector.getVariableDescriptor();
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
    public Iterator<ElementRef> iterator() {
        Iterator<Object> iterator = valueSelector.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ElementRef next() {
                return elementRef(iterator.next());
            }
        };
    }

    @Override
    public boolean isCountable() {
        return valueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return valueSelector.isNeverEnding();
    }

    private ElementRef elementRef(Object element) {
        return ElementRef.of(inverseVariableSupply.getInverseSingleton(element), indexVariableSupply.getIndex(element));
    }
}
