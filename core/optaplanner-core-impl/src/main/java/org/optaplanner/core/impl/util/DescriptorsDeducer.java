package org.optaplanner.core.impl.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.optaplanner.core.config.AbstractConfig;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;

public class DescriptorsDeducer<Solution_, Config_ extends AbstractConfig<Config_>> {
    private final AbstractConfig<Config_> config;

    public DescriptorsDeducer(AbstractConfig<Config_> config) {
        this.config = config;
    }

    public List<GenuineVariableDescriptor<Solution_>> deduceVariableDescriptorList(
            EntityDescriptor<Solution_> entityDescriptor, List<String> variableNameIncludeList) {
        Objects.requireNonNull(entityDescriptor);
        List<GenuineVariableDescriptor<Solution_>> variableDescriptorList =
                entityDescriptor.getGenuineVariableDescriptorList();
        return (List<GenuineVariableDescriptor<Solution_>>) deduceDescriptors(entityDescriptor, variableNameIncludeList,
                variableDescriptorList);
    }

    public Collection<ShadowVariableDescriptor<Solution_>> deduceShadowVariableDescriptorList(
            EntityDescriptor<Solution_> entityDescriptor, List<String> variableNameIncludeList) {
        Objects.requireNonNull(entityDescriptor);
        Collection<ShadowVariableDescriptor<Solution_>> variableDescriptorList =
                entityDescriptor.getShadowVariableDescriptors();
        return (Collection<ShadowVariableDescriptor<Solution_>>) deduceDescriptors(entityDescriptor, variableNameIncludeList,
                variableDescriptorList);
    }

    private Collection<? extends VariableDescriptor<Solution_>> deduceDescriptors(EntityDescriptor<Solution_> entityDescriptor,
            List<String> variableNameIncludeList, Collection<? extends VariableDescriptor<Solution_>> variableDescriptorList) {
        if (variableNameIncludeList == null) {
            return variableDescriptorList;
        }

        return variableNameIncludeList.stream()
                .map(variableNameInclude -> variableDescriptorList.stream()
                        .filter(variableDescriptor -> variableDescriptor.getVariableName().equals(variableNameInclude))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The config (" + config
                                + ") has a variableNameInclude (" + variableNameInclude
                                + ") which does not exist in the entity (" + entityDescriptor.getEntityClass()
                                + ")'s variableDescriptorList (" + variableDescriptorList + ").")))
                .collect(Collectors.toList());
    }
}
