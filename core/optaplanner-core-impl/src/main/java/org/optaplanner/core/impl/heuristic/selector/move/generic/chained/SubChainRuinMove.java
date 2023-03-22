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
public class SubChainRuinMove<Solution_> extends AbstractMove<Solution_> {

    private final SubChain subChain;
    private final GenuineVariableDescriptor<Solution_> variableDescriptor;
    private final Object oldTrailingLastEntity;

    public SubChainRuinMove(SubChain subChain, GenuineVariableDescriptor<Solution_> variableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply) {
        assert variableDescriptor.isChained() : "SubChainRuinMove: variable descriptor must be chained";
        this.subChain = subChain;
        this.variableDescriptor = variableDescriptor;
        oldTrailingLastEntity = inverseVariableSupply.getInverseSingleton(subChain.getLastEntity());
    }

    private SubChainRuinMove(SubChain subChain, GenuineVariableDescriptor<Solution_> variableDescriptor,
            Object oldTrailingLastEntity) {
        this.subChain = subChain;
        this.variableDescriptor = variableDescriptor;
        this.oldTrailingLastEntity = oldTrailingLastEntity;
    }

    public SubChain getSubChain() {
        return subChain;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return !subChain.getEntityList().isEmpty() && subChain.getEntityList().stream()
                .anyMatch(entity -> variableDescriptor.isInitialized(entity) && variableDescriptor.getValue(entity) != null);
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        throw new UnsupportedOperationException("Undoing a sub chain ruin move is unsupported.");
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        Object firstEntity = subChain.getFirstEntity();
        Object oldFirstValue = variableDescriptor.getValue(firstEntity);
        // Close the chain
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        if (oldTrailingLastEntity != null) {
            innerScoreDirector.changeVariableFacade(variableDescriptor, oldTrailingLastEntity, oldFirstValue);
        }
        // Ruin the disconnected entities
        for (Object entity : subChain.getEntityList()) {
            innerScoreDirector.changeVariableFacade(variableDescriptor, entity, null);
        }
    }

    @Override
    public SubChainRuinMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new SubChainRuinMove<>(subChain.rebase(destinationScoreDirector),
                variableDescriptor,
                destinationScoreDirector.lookUpWorkingObject(oldTrailingLastEntity));
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return subChain.getEntityList();
    }

    @Override
    public Collection<?> getPlanningValues() {
        return Collections.singletonList(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SubChainRuinMove<?> other = (SubChainRuinMove<?>) o;
        return Objects.equals(subChain, other.subChain) &&
                Objects.equals(variableDescriptor, other.variableDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subChain, variableDescriptor);
    }

    @Override
    public String toString() {
        return subChain.toDottedString() + " {" + subChain.toDottedString() + " -> " + null + "}";
    }

}
