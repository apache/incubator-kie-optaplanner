package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.FilteringEntitySelector;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class SimpleCompositeRuinMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    protected final FilteringEntitySelector<Solution_> originEntitySelector;
    protected final List<GenuineVariableDescriptor<Solution_>> variableDescriptorList;
    protected final Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors;
    protected final Integer percentageToBeRuined;
    protected final HashMap<GenuineVariableDescriptor<Solution_>, SingletonInverseVariableSupply> variableDescriptorToInverseVariableSupply =
            new HashMap<>();
    private final boolean chained;

    protected final InitializedSelectionFilter<Solution_> initializedFilter;

    public SimpleCompositeRuinMoveSelector(EntitySelector<Solution_> entitySelector,
            List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors, Integer percentageToBeRuined) {
        if (percentageToBeRuined <= 0 || percentageToBeRuined > 100) {
            throw new IllegalArgumentException(
                    "CompositeRuinMoveSelector: percentage to ruin the solution must be greater than 0 and smaller or equal to 100%");
        }
        this.initializedFilter = new InitializedSelectionFilter<>(entitySelector.getEntityDescriptor());
        this.originEntitySelector =
                new FilteringEntitySelector<>(entitySelector, Collections.singletonList(initializedFilter));
        this.variableDescriptorList = variableDescriptorList;
        this.shadowVariableDescriptors = shadowVariableDescriptors;
        this.percentageToBeRuined = percentageToBeRuined;
        this.chained = this.variableDescriptorList.stream().anyMatch(GenuineVariableDescriptor::isChained);

        phaseLifecycleSupport.addEventListener(entitySelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return originEntitySelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return true;
    }

    @Override
    public long getSize() {
        return originEntitySelector.getSize();
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        if (chained) {
            SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
            for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                if (variableDescriptor.isChained()) {
                    variableDescriptorToInverseVariableSupply.put(variableDescriptor,
                            supplyManager.demand(new SingletonInverseVariableDemand<>(variableDescriptor)));
                }
            }
        }
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        if (chained) {
            variableDescriptorToInverseVariableSupply.clear();
        }
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        long numberOfRuinableEntities = getSize();
        return new SimpleRuinMoveIterator<>(originEntitySelector,
                variableDescriptorList,
                shadowVariableDescriptors, variableDescriptorToInverseVariableSupply, numberOfRuinableEntities,
                percentageToBeRuined * numberOfRuinableEntities / 100);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + originEntitySelector + ")";
    }
}
