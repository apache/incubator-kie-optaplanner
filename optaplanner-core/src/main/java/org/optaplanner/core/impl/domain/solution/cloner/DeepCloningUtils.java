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

package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.Pair;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.impl.domain.common.ReflectionHelper;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

public final class DeepCloningUtils {
    // owningClass != declaringClass
    // owningClass refers to the actual instance class, which may be a subclass of the declaring class

    /**
     * Gets the memoized deep cloning decision from the provided
     * memoization maps, computing them if neccessary.
     *
     * @param solutionDescriptor the solution descriptor for the solution class
     * @param field the field to get the deep cloning decision of
     * @param owningClass the class that owns the field; can be different
     *        from the field's declaring class (ex: subclass)
     * @param actualValueClass the class of the value that is currently assigned
     *        to the field; can be different from the field type
     *        (ex: for the field "List myList", the actual value
     *        class might be ArrayList).
     * @param fieldDeepClonedMemoization memoization map for (field, owningClass) decisions
     * @param actualValueClassDeepClonedMemoization memoization maps for actualValueClass decisions
     * @return true iff the field should be deep cloned with a particular value.
     */
    public static boolean getMemorizedDeepCloneDecision(final SolutionDescriptor<?> solutionDescriptor,
            Field field, Class<?> owningClass,
            Class<?> actualValueClass,
            ConcurrentMap<Pair<Field, Class<?>>, Boolean> fieldDeepClonedMemoization,
            ConcurrentMap<Class<?>, Boolean> actualValueClassDeepClonedMemoization) {
        Pair<Field, Class<?>> pair = Pair.of(field, owningClass);
        Boolean deepCloneDecision = fieldDeepClonedMemoization.computeIfAbsent(pair,
                key -> DeepCloningUtils.isFieldDeepCloned(solutionDescriptor, field, owningClass));
        return deepCloneDecision || actualValueClassDeepClonedMemoization.computeIfAbsent(actualValueClass,
                key -> isClassDeepCloned(solutionDescriptor, actualValueClass));
    }

    /**
     * Gets the deep cloning decision for a field with a particular value assigned to it.
     *
     * @param solutionDescriptor The solution descriptor for the solution class
     * @param field The field to get the deep cloning decision of
     * @param owningClass The class that owns the field; can be different
     *        from the field's declaring class (ex: subclass).
     * @param actualValueClass The class of the value that is currently assigned
     *        to the field; can be different from the field type
     *        (ex: for the field "List myList", the actual value
     *        class might be ArrayList).
     * @return True iff the field should be deep cloned with a particular value.
     */
    public static boolean getDeepCloneDecision(final SolutionDescriptor<?> solutionDescriptor,
            Field field, Class<?> owningClass,
            Class<?> actualValueClass) {
        return isFieldDeepCloned(solutionDescriptor, field, owningClass) || isClassDeepCloned(solutionDescriptor,
                actualValueClass);
    }

    /**
     * Gets the deep cloning decision for a field.
     *
     * @param solutionDescriptor The solution descriptor for the solution class
     * @param field The field to get the deep cloning decision of
     * @param owningClass The class that owns the field; can be different
     *        from the field's declaring class (ex: subclass).
     * @return True iff the field should always be deep cloned (regardless of value).
     */
    public static boolean isFieldDeepCloned(final SolutionDescriptor<?> solutionDescriptor,
            Field field, Class<?> owningClass) {
        if (field.getType().isEnum()) {
            return false;
        }
        return isFieldAnEntityPropertyOnSolution(solutionDescriptor, field, owningClass)
                || isFieldAnEntityOrSolution(solutionDescriptor, field)
                || isFieldADeepCloneProperty(field, owningClass);
    }

    /**
     * Return true only if a field represent an entity property on the solution class.
     * An entity property is one who type is a PlanningEntity or a collection
     * of PlanningEntity.
     *
     * @param solutionDescriptor The solution descriptor for the solution class
     * @param field The field to get the deep cloning decision of
     * @param owningClass The class that owns the field; can be different
     *        from the field's declaring class (ex: subclass).
     * @return True only if the field is an entity property on the solution class.
     *         May return false if the field getter/setter is complex.
     */
    public static boolean isFieldAnEntityPropertyOnSolution(final SolutionDescriptor<?> solutionDescriptor,
            Field field, Class<?> owningClass) {
        // field.getDeclaringClass() is a superclass of or equal to the owningClass
        if (solutionDescriptor.getSolutionClass().isAssignableFrom(owningClass)) {
            String fieldName = field.getName();
            // This assumes we're dealing with a simple getter/setter.
            // If that assumption is false, validateCloneSolution(...) fails-fast.
            if (solutionDescriptor.getEntityMemberAccessorMap().get(fieldName) != null) {
                return true;
            }
            // This assumes we're dealing with a simple getter/setter.
            // If that assumption is false, validateCloneSolution(...) fails-fast.
            if (solutionDescriptor.getEntityCollectionMemberAccessorMap().get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true iff a field represent an Entity/Solution or a collection
     * of Entity/Solution.
     *
     * @param solutionDescriptor The solution descriptor for the solution class
     * @param field The field to get the deep cloning decision of
     * @return True only if the field represents or contains a PlanningEntity or PlanningSolution
     */
    public static boolean isFieldAnEntityOrSolution(final SolutionDescriptor<?> solutionDescriptor,
            Field field) {
        Class<?> type = field.getType();
        if (isClassDeepCloned(solutionDescriptor, type)) {
            return true;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            if (isTypeArgumentDeepCloned(solutionDescriptor, field.getGenericType())) {
                return true;
            }
        } else if (type.isArray()) {
            if (isClassDeepCloned(solutionDescriptor, type.getComponentType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClassDeepCloned(final SolutionDescriptor<?> solutionDescriptor,
            Class<?> type) {
        return solutionDescriptor.hasEntityDescriptor(type)
                || solutionDescriptor.getSolutionClass().isAssignableFrom(type)
                || type.isAnnotationPresent(DeepPlanningClone.class);
    }

    public static boolean isTypeArgumentDeepCloned(final SolutionDescriptor<?> solutionDescriptor,
            Type genericType) {
        // Check the generic type arguments of the field.
        // Yes, it is possible for fields and methods, but not instances!
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                if (actualTypeArgument instanceof Class
                        && isClassDeepCloned(solutionDescriptor, (Class) actualTypeArgument)) {
                    return true;
                }
                if (isTypeArgumentDeepCloned(solutionDescriptor, actualTypeArgument)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFieldADeepCloneProperty(Field field, Class<?> owningClass) {
        if (field.isAnnotationPresent(DeepPlanningClone.class)) {
            return true;
        }
        Method getterMethod = ReflectionHelper.getGetterMethod(owningClass, field.getName());
        if (getterMethod != null && getterMethod.isAnnotationPresent(DeepPlanningClone.class)) {
            return true;
        }
        return false;
    }

    // ************************************************************************
    // Private constructor
    // ************************************************************************

    private DeepCloningUtils() {
    }
}
