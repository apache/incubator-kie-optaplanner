/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.domain.variable.listener.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.SourcedVariableListener;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

final class NotifiableRegistry<Solution_> {

    private final InnerScoreDirector<Solution_, ?> scoreDirector;
    private final List<VariableListenerNotifiable<Solution_>> notifiableList = new ArrayList<>();
    private final Map<EntityDescriptor<?>, List<VariableListenerNotifiable<Solution_>>> sourceEntityToNotifiableMap =
            new LinkedHashMap<>();
    private final Map<VariableDescriptor<?>, List<VariableListenerNotifiable<Solution_>>> sourceVariableToNotifiableMap =
            new LinkedHashMap<>();
    private int nextGlobalOrder = 0;

    NotifiableRegistry(InnerScoreDirector<Solution_, ?> scoreDirector) {
        this.scoreDirector = scoreDirector;
        for (EntityDescriptor<Solution_> entityDescriptor : scoreDirector.getSolutionDescriptor().getEntityDescriptors()) {
            sourceEntityToNotifiableMap.put(entityDescriptor, new ArrayList<>());
            for (VariableDescriptor<Solution_> variableDescriptor : entityDescriptor.getDeclaredVariableDescriptors()) {
                sourceVariableToNotifiableMap.put(variableDescriptor, new ArrayList<>());
            }
        }
    }

    void registerShadowVariableListener(ShadowVariableDescriptor<Solution_> shadowVariableDescriptor,
            VariableListener<Solution_, ?> variableListener) {
        int globalOrder = shadowVariableDescriptor.getGlobalShadowOrder();
        VariableListenerNotifiable<Solution_> notifiable =
                new VariableListenerNotifiable<>(scoreDirector, variableListener, globalOrder);
        for (VariableDescriptor<Solution_> source : shadowVariableDescriptor.getSourceVariableDescriptorList()) {
            registerNotifiable(source, notifiable);
        }
        notifiableList.add(notifiable);
        nextGlobalOrder = Math.max(nextGlobalOrder, globalOrder + 1);
    }

    void registerSourcedVariableListener(SourcedVariableListener<Solution_, ?> variableListener) {
        VariableListenerNotifiable<Solution_> notifiable =
                new VariableListenerNotifiable<>(scoreDirector, variableListener, nextGlobalOrder);
        VariableDescriptor<Solution_> source = variableListener.getSourceVariableDescriptor();
        registerNotifiable(source, notifiable);
        notifiableList.add(notifiable);
        nextGlobalOrder++;
    }

    private void registerNotifiable(VariableDescriptor<?> source, VariableListenerNotifiable<Solution_> notifiable) {
        sourceVariableToNotifiableMap.get(source).add(notifiable);
        List<VariableListenerNotifiable<Solution_>> entityNotifiableList = sourceEntityToNotifiableMap
                .get(source.getEntityDescriptor());
        if (!entityNotifiableList.contains(notifiable)) {
            entityNotifiableList.add(notifiable);
        }
    }

    void sort() {
        Collections.sort(notifiableList);
    }

    Iterable<VariableListenerNotifiable<Solution_>> getAll() {
        return notifiableList;
    }

    List<VariableListenerNotifiable<Solution_>> get(EntityDescriptor<Solution_> entityDescriptor) {
        return sourceEntityToNotifiableMap.get(entityDescriptor);
    }

    List<VariableListenerNotifiable<Solution_>> get(VariableDescriptor<Solution_> variableDescriptor) {
        return sourceVariableToNotifiableMap.getOrDefault(variableDescriptor,
                Collections.emptyList()); // Avoids null for chained swap move on an unchained var.
    }
}
