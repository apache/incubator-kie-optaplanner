/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.solution.cloner.gizmo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.domain.common.accessor.gizmo.GizmoMemberDescriptor;
import org.optaplanner.core.impl.domain.solution.cloner.DeepCloningUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

public class GizmoSolutionOrEntityDescriptor {
    SolutionDescriptor<?> solutionDescriptor;
    Map<Field, GizmoMemberDescriptor> solutionFieldToMemberDescriptorMap;
    final Map<Class<?>, GizmoSolutionOrEntityDescriptor> memoizedGizmoSolutionOrEntityDescriptorForClassMap;
    Set<Field> deepClonedFields;
    Set<Field> shallowlyClonedFields;

    public GizmoSolutionOrEntityDescriptor(SolutionDescriptor<?> solutionDescriptor) {
        this.solutionDescriptor = solutionDescriptor;
        solutionFieldToMemberDescriptorMap = new HashMap<>();
        addFieldsToSolutionFieldToMemberDescriptorMap(solutionDescriptor.getSolutionClass(),
                solutionFieldToMemberDescriptorMap);
        deepClonedFields = new HashSet<>();
        shallowlyClonedFields = new HashSet<>();

        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (DeepCloningUtils.getDeepCloneDecision(solutionDescriptor, field, solutionDescriptor.getSolutionClass(),
                    field.getType())) {
                deepClonedFields.add(field);
            } else {
                shallowlyClonedFields.add(field);
            }
        }

        memoizedGizmoSolutionOrEntityDescriptorForClassMap = new HashMap<>();
    }

    public GizmoSolutionOrEntityDescriptor(SolutionDescriptor<?> solutionDescriptor, Class<?> entityClass) {
        this.solutionDescriptor = solutionDescriptor;
        solutionFieldToMemberDescriptorMap = new HashMap<>();
        addFieldsToSolutionFieldToMemberDescriptorMap(entityClass, solutionFieldToMemberDescriptorMap);
        deepClonedFields = new HashSet<>();
        shallowlyClonedFields = new HashSet<>();

        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (DeepCloningUtils.getDeepCloneDecision(solutionDescriptor, field, entityClass, field.getType())) {
                deepClonedFields.add(field);
            } else {
                shallowlyClonedFields.add(field);
            }
        }

        memoizedGizmoSolutionOrEntityDescriptorForClassMap = new HashMap<>();
    }

    public GizmoSolutionOrEntityDescriptor(Class<?> holderClass, SolutionDescriptor<?> solutionDescriptor,
            Map<Field, GizmoMemberDescriptor> solutionFieldToMemberDescriptorMap,
            Map<Class<?>, GizmoSolutionOrEntityDescriptor> memoizedGizmoSolutionOrEntityDescriptorForClassMap) {
        this.solutionDescriptor = solutionDescriptor;
        this.solutionFieldToMemberDescriptorMap = solutionFieldToMemberDescriptorMap;
        deepClonedFields = new HashSet<>();
        shallowlyClonedFields = new HashSet<>();

        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (DeepCloningUtils.getDeepCloneDecision(solutionDescriptor, field, holderClass, field.getType())) {
                deepClonedFields.add(field);
            } else {
                shallowlyClonedFields.add(field);
            }
        }
        this.memoizedGizmoSolutionOrEntityDescriptorForClassMap = memoizedGizmoSolutionOrEntityDescriptorForClassMap;
    }

    private static void addFieldsToSolutionFieldToMemberDescriptorMap(Class<?> clazz,
            Map<Field, GizmoMemberDescriptor> solutionFieldToMemberDescriptorMap) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                solutionFieldToMemberDescriptorMap.put(field, new GizmoMemberDescriptor(field));
            }
        }
        if (clazz.getSuperclass() != null) {
            addFieldsToSolutionFieldToMemberDescriptorMap(clazz.getSuperclass(), solutionFieldToMemberDescriptorMap);
        }
    }

    public SolutionDescriptor<?> getSolutionDescriptor() {
        return solutionDescriptor;
    }

    public Set<GizmoMemberDescriptor> getShallowClonedMemberDescriptors() {
        return solutionFieldToMemberDescriptorMap.keySet().stream()
                .filter(field -> shallowlyClonedFields.contains(field))
                .map(solutionFieldToMemberDescriptorMap::get).collect(Collectors.toSet());
    }

    public Set<Field> getDeepClonedFields() {
        return deepClonedFields;
    }

    public GizmoMemberDescriptor getMemberDescriptorForField(Field field) {
        return solutionFieldToMemberDescriptorMap.get(field);
    }

    public GizmoSolutionOrEntityDescriptor getSolutionOrEntityDescriptorForClass(Class<?> clazz) {
        return memoizedGizmoSolutionOrEntityDescriptorForClassMap
                .computeIfAbsent(clazz,
                        theClass -> new GizmoSolutionOrEntityDescriptor(solutionDescriptor, theClass));
    }

}
