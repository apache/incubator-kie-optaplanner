package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.domain.common.accessor.gizmo.GizmoMemberDescriptor;
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

        // Methods for determining what fields are deep cloned are not static :(
        FieldAccessingSolutionCloner cloner = new FieldAccessingSolutionCloner(solutionDescriptor);
        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (cloner.retrieveDeepCloneDecision(field, field.getType(), field.getType())) {
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

        // Methods for determining what fields are deep cloned are not static :(
        FieldAccessingSolutionCloner cloner = new FieldAccessingSolutionCloner(solutionDescriptor);
        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (cloner.retrieveDeepCloneDecision(field, field.getType(), field.getType())) {
                deepClonedFields.add(field);
            } else {
                shallowlyClonedFields.add(field);
            }
        }

        memoizedGizmoSolutionOrEntityDescriptorForClassMap = new HashMap<>();
    }

    public GizmoSolutionOrEntityDescriptor(SolutionDescriptor<?> solutionDescriptor,
            Map<Field, GizmoMemberDescriptor> solutionFieldToMemberDescriptorMap,
            Map<Class<?>, GizmoSolutionOrEntityDescriptor> memoizedGizmoSolutionOrEntityDescriptorForClassMap) {
        this.solutionDescriptor = solutionDescriptor;
        this.solutionFieldToMemberDescriptorMap = solutionFieldToMemberDescriptorMap;
        deepClonedFields = new HashSet<>();
        shallowlyClonedFields = new HashSet<>();

        // Methods for determining what fields are deep cloned are not static :(
        FieldAccessingSolutionCloner cloner = new FieldAccessingSolutionCloner(solutionDescriptor);
        for (Field field : solutionFieldToMemberDescriptorMap.keySet()) {
            if (cloner.retrieveDeepCloneDecision(field, field.getType(), field.getType())) {
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
