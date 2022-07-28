package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableDemand;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonListInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.AbstractSelector;
import org.optaplanner.core.impl.heuristic.selector.ListIterableSelector;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheLifecycleBridge;
import org.optaplanner.core.impl.heuristic.selector.common.SelectionCacheLifecycleListener;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class CachedElementSelector<Solution_> extends AbstractSelector<Solution_>
        implements ListIterableSelector<Solution_, ElementRef>, SelectionCacheLifecycleListener<Solution_> {

    private static final SelectionCacheType CACHE_TYPE = SelectionCacheType.STEP;

    private final EntityIndependentValueSelector<Solution_> valueSelector;
    private final ListVariableDescriptor<Solution_> listVariableDescriptor;

    private SingletonInverseVariableSupply inverseVariableSupply;
    private IndexVariableSupply indexVariableSupply;

    private List<ElementRef> elementRefsCache;

    public CachedElementSelector(EntityIndependentValueSelector<Solution_> valueSelector) {
        this.valueSelector = valueSelector;
        if (valueSelector.isNeverEnding()) {
            throw new IllegalArgumentException("The child valueSelector (" + valueSelector + ") must not be never ending.");
        }
        if (!valueSelector.isCountable()) {
            throw new IllegalArgumentException("The child valueSelector (" + valueSelector + ") must be countable.");
        }
        listVariableDescriptor = (ListVariableDescriptor<Solution_>) valueSelector.getVariableDescriptor();
        phaseLifecycleSupport.addEventListener(valueSelector);
        phaseLifecycleSupport.addEventListener(new SelectionCacheLifecycleBridge<>(CACHE_TYPE, this));
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
    public SelectionCacheType getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    public void constructCache(SolverScope<Solution_> solverScope) {
        if (elementRefsCache == null) {
            elementRefsCache = new ArrayList<>(intSize(valueSelector));
        }
        for (Object element : valueSelector) {
            elementRefsCache.add(elementRef(element));
        }
    }

    @Override
    public void disposeCache(SolverScope<Solution_> solverScope) {
        elementRefsCache.clear();
    }

    @Override
    public long getSize() {
        return valueSelector.getSize();
    }

    @Override
    public Iterator<ElementRef> iterator() {
        return elementRefsCache.iterator();
    }

    @Override
    public boolean isCountable() {
        return true;
    }

    @Override
    public boolean isNeverEnding() {
        return false;
    }

    @Override
    public ListIterator<ElementRef> listIterator() {
        return elementRefsCache.listIterator();
    }

    @Override
    public ListIterator<ElementRef> listIterator(int index) {
        return elementRefsCache.listIterator(index);
    }

    private int intSize(EntityIndependentValueSelector<Solution_> valueSelector) {
        long valueSize = valueSelector.getSize();
        if (valueSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a valueSelector (" + valueSelector
                    + ") with valueSize (" + valueSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        return Math.toIntExact(valueSize);
    }

    private ElementRef elementRef(Object element) {
        return ElementRef.of(inverseVariableSupply.getInverseSingleton(element), indexVariableSupply.getIndex(element));
    }
}
