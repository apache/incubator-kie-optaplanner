package org.optaplanner.core.impl.domain.variable.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.domain.variable.ShadowVariable;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class FooShadowVariableDescriptor<Solution_> extends ShadowVariableDescriptor<Solution_> {

    private final Map<Class<? extends AbstractVariableListener>, List<VariableDescriptor<Solution_>>> listenerClassToSourceDescriptorListMap =
            new HashMap<>();

    public FooShadowVariableDescriptor(EntityDescriptor<Solution_> entityDescriptor,
            MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
    }

    @Override
    public void processAnnotations(DescriptorPolicy descriptorPolicy) {
        // Do nothing
    }

    @Override
    public void linkVariableDescriptors(DescriptorPolicy descriptorPolicy) {
        for (ShadowVariable shadowVariable : variableMemberAccessor.getDeclaredAnnotationsByType(ShadowVariable.class)) {
            linkSource(shadowVariable);
        }
    }

    private void linkSource(ShadowVariable shadowVariable) {
        EntityDescriptor<Solution_> sourceEntityDescriptor;
        Class<?> sourceEntityClass = shadowVariable.sourceEntityClass();
        if (sourceEntityClass.equals(ShadowVariable.NullEntityClass.class)) {
            sourceEntityDescriptor = entityDescriptor;
        } else {
            sourceEntityDescriptor = entityDescriptor.getSolutionDescriptor().findEntityDescriptor(sourceEntityClass);
            if (sourceEntityDescriptor == null) {
                throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                        + ") has a @" + ShadowVariable.class.getSimpleName()
                        + " annotated property (" + variableMemberAccessor.getName()
                        + ") with a sourceEntityClass (" + sourceEntityClass
                        + ") which is not a valid planning entity.");
            }
        }
        String sourceVariableName = shadowVariable.sourceVariableName();
        VariableDescriptor<Solution_> sourceVariableDescriptor =
                sourceEntityDescriptor.getVariableDescriptor(sourceVariableName);
        if (sourceVariableDescriptor == null) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + ShadowVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with sourceVariableName (" + sourceVariableName
                    + ") which is not a valid planning variable on entityClass ("
                    + sourceEntityDescriptor.getEntityClass() + ").\n"
                    + sourceEntityDescriptor.buildInvalidVariableNameExceptionMessage(sourceVariableName));
        }
        // TODO list source var => list var listener
        // TODO basic source var => basic var listener
        sourceVariableDescriptor.registerSinkVariableDescriptor(this);
        listenerClassToSourceDescriptorListMap
                .computeIfAbsent(shadowVariable.variableListenerClass(), variableListenerClass -> new ArrayList<>())
                .add(sourceVariableDescriptor);
    }

    @Override
    public List<VariableDescriptor<Solution_>> getSourceVariableDescriptorList() {
        return listenerClassToSourceDescriptorListMap.values().stream().flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Class<? extends AbstractVariableListener>> getVariableListenerClasses() {
        return listenerClassToSourceDescriptorListMap.keySet();
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public Demand<?> getProvidedDemand() {
        throw new UnsupportedOperationException("Custom shadow variable listener will never be demanded.");
    }

    @Override
    public Iterable<ListenerSources<Solution_>> buildVariableListener(InnerScoreDirector<Solution_, ?> scoreDirector) {
        return listenerClassToSourceDescriptorListMap.entrySet().stream().map(classListEntry -> {
            AbstractVariableListener<Solution_, Object> variableListener =
                    ConfigUtils.newInstance(this::toString, "variableListenerClass", classListEntry.getKey());
            return new ListenerSources<>(variableListener, classListEntry.getValue());
        }).collect(Collectors.toList());
    }
}
