package org.optaplanner.core.impl.domain.variable.custom;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.domain.variable.PiggybackShadowVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.VariableListenerWithSources;
import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class PiggybackShadowVariableDescriptor<Solution_> extends ShadowVariableDescriptor<Solution_> {

    protected FooShadowVariableDescriptor<Solution_> refVariableDescriptor;

    public PiggybackShadowVariableDescriptor(EntityDescriptor<Solution_> entityDescriptor,
            MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
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
        PiggybackShadowVariable piggybackShadowVariable = variableMemberAccessor.getAnnotation(PiggybackShadowVariable.class);
        EntityDescriptor<Solution_> refEntityDescriptor;
        Class<?> refEntityClass = piggybackShadowVariable.entityClass();
        if (refEntityClass.equals(PiggybackShadowVariable.NullEntityClass.class)) {
            refEntityDescriptor = entityDescriptor;
        } else {
            refEntityDescriptor = entityDescriptor.getSolutionDescriptor().findEntityDescriptor(refEntityClass);
            if (refEntityDescriptor == null) {
                throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                        + ") has a @" + PiggybackShadowVariable.class.getSimpleName()
                        + " annotated property (" + variableMemberAccessor.getName()
                        + ") with a refEntityClass (" + refEntityClass
                        + ") which is not a valid planning entity.");
            }
        }
        String refVariableName = piggybackShadowVariable.variableName();
        VariableDescriptor<Solution_> uncastRefVariableDescriptor = refEntityDescriptor.getVariableDescriptor(refVariableName);
        if (uncastRefVariableDescriptor == null) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + PiggybackShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with refVariableName (" + refVariableName
                    + ") which is not a valid planning variable on entityClass ("
                    + refEntityDescriptor.getEntityClass() + ").\n"
                    + refEntityDescriptor.buildInvalidVariableNameExceptionMessage(refVariableName));
        }
        if (!(uncastRefVariableDescriptor instanceof FooShadowVariableDescriptor)) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + PiggybackShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with refVariable (" + uncastRefVariableDescriptor.getSimpleEntityAndVariableName()
                    + ") that lacks a @" + ShadowVariable.class.getSimpleName() + " annotation.");
        }
        refVariableDescriptor = (FooShadowVariableDescriptor<Solution_>) uncastRefVariableDescriptor;
        refVariableDescriptor.registerSinkVariableDescriptor(this);
    }

    @Override
    public List<VariableDescriptor<Solution_>> getSourceVariableDescriptorList() {
        return Collections.singletonList(refVariableDescriptor);
    }

    @Override
    public Collection<Class<? extends AbstractVariableListener>> getVariableListenerClasses() {
        return refVariableDescriptor.getVariableListenerClasses();
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public Demand<?> getProvidedDemand() {
        throw new UnsupportedOperationException("Custom shadow variable cannot be demanded.");
    }

    @Override
    public boolean hasVariableListener() {
        return false;
    }

    @Override
    public Iterable<VariableListenerWithSources<Solution_>> buildVariableListeners(SupplyManager supplyManager) {
        throw new UnsupportedOperationException("The piggybackShadowVariableDescriptor (" + this
                + ") cannot build a variable listener.");
    }
}
