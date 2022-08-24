package org.optaplanner.core.impl.domain.variable.previous;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.api.domain.variable.PreviousElementShadowVariable;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class PreviousElementShadowVariableDescriptor<Solution_> extends ShadowVariableDescriptor<Solution_> {

    protected ListVariableDescriptor<Solution_> sourceVariableDescriptor;

    public PreviousElementShadowVariableDescriptor(
            EntityDescriptor<Solution_> entityDescriptor,
            MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
        if (!variableMemberAccessor.getType().equals(entityDescriptor.getEntityClass())) {
            throw new IllegalStateException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has an @" + PreviousElementShadowVariableDescriptor.class.getSimpleName()
                    + " annotated member (" + variableMemberAccessor
                    + ") of type (" + variableMemberAccessor.getType()
                    + ") which is not the entityClass type.");
        }
    }

    @Override
    public void processAnnotations(DescriptorPolicy descriptorPolicy) {
        // Do nothing
    }

    @Override
    public void linkVariableDescriptors(DescriptorPolicy descriptorPolicy) {
        linkShadowSources(descriptorPolicy);
    }

    private void linkShadowSources(DescriptorPolicy descriptorPolicy) {
        String sourceVariableName =
                variableMemberAccessor.getAnnotation(PreviousElementShadowVariable.class).sourceVariableName();
        List<EntityDescriptor<Solution_>> entitiesWithSourceVariable =
                entityDescriptor.getSolutionDescriptor().getEntityDescriptors().stream()
                        .filter(entityDescriptor -> entityDescriptor.hasVariableDescriptor(sourceVariableName))
                        .collect(Collectors.toList());
        if (entitiesWithSourceVariable.isEmpty()) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has an @" + PreviousElementShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with sourceVariableName (" + sourceVariableName
                    + ") which is not a valid planning variable on any of the entity classes ("
                    + entityDescriptor.getSolutionDescriptor().getEntityDescriptors() + ").");
        }
        if (entitiesWithSourceVariable.size() > 1) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has an @" + PreviousElementShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with sourceVariableName (" + sourceVariableName
                    + ") which is not a unique planning variable."
                    + " A planning variable with the name (" + sourceVariableName + ") exists on multiple entity classes ("
                    + entitiesWithSourceVariable + ").");
        }
        VariableDescriptor<Solution_> variableDescriptor =
                entitiesWithSourceVariable.get(0).getVariableDescriptor(sourceVariableName);
        if (variableDescriptor == null) {
            throw new IllegalStateException(
                    "Impossible state: variableDescriptor (" + variableDescriptor + ") is null"
                            + " but previous checks indicate that the entityClass (" + entitiesWithSourceVariable.get(0)
                            + ") has a planning variable with sourceVariableName (" + sourceVariableName + ").");
        }
        if (!(variableDescriptor instanceof ListVariableDescriptor)) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has an @" + PreviousElementShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with sourceVariableName (" + sourceVariableName
                    + ") which is not a @" + PlanningListVariable.class.getSimpleName() + ".");
        }
        sourceVariableDescriptor = (ListVariableDescriptor<Solution_>) variableDescriptor;
        sourceVariableDescriptor.registerSinkVariableDescriptor(this);
    }

    @Override
    public List<VariableDescriptor<Solution_>> getSourceVariableDescriptorList() {
        return Collections.singletonList(sourceVariableDescriptor);
    }

    @Override
    public Class<PreviousElementVariableListener> getVariableListenerClass() {
        return PreviousElementVariableListener.class;
    }

    @Override
    public Demand<?> getProvidedDemand() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PreviousElementVariableListener<Solution_> buildVariableListener(InnerScoreDirector<Solution_, ?> scoreDirector) {
        return new PreviousElementVariableListener<>(this, sourceVariableDescriptor);
    }
}
