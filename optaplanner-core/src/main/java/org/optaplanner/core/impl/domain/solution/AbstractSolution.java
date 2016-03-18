/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.solution;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningEntityProperty;
import org.optaplanner.core.api.domain.solution.PlanningFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.domain.common.ReflectionHelper;

/**
 * Currently only used by OptaPlanner Workbench.
 * TODO Should we promote using this class in the examples and docs too?
 * We can never enforce it, as the user might want to use a different superclass.
 * @param <S> the {@link Score} type used by this use case
 */
public abstract class AbstractSolution<S extends Score> implements Serializable {

    protected S score;

    @PlanningScore
    public S getScore() {
        return score;
    }

    public void setScore(S score) {
        this.score = score;
    }

    /**
     * Convenience method for tests.
     *
     * @return All entities from anywhere in this class' hierarchy.
     */
    @PlanningFactCollectionProperty
    protected Collection<?> getProblemFacts() {
        Class<? extends AbstractSolution> instanceClass = getClass();
        return getProblemFactsFromClass(instanceClass);
    }

    private Collection<Object> getProblemFactsFromClass(Class<?> instanceClass) {
        Collection<Object> factList = new ArrayList<>();
        if (instanceClass.equals(AbstractSolution.class)) {
            // The field score should not be included
            return factList;
        }
        for (Field field : instanceClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (isFieldAPlanningEntityPropertyOrPlanningEntityCollectionProperty(field, instanceClass)) {
                continue;
            }
            Object value;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("The class (" + instanceClass + ") has a field (" + field
                        + ") which can not be read to create the problem facts.", e);
            }
            if (value != null) {
                if (value instanceof Collection) {
                    factList.addAll((Collection) value);
                } else if (value instanceof Map) {
                    throw new IllegalStateException("The class (" + instanceClass + ") has a field (" + field
                            + ") which is a " + Map.class.getSimpleName() + " and that's not yet supported.");
                } else {
                    factList.add(value);
                }
            }
        }
        Class<?> superclass = instanceClass.getSuperclass();
        if (superclass != null) {
            factList.addAll(getProblemFactsFromClass(superclass));
        }
        return factList;
    }

    private boolean isFieldAPlanningEntityPropertyOrPlanningEntityCollectionProperty(Field field,
            Class fieldInstanceClass) {
        if (field.isAnnotationPresent(PlanningEntityProperty.class)
                || field.isAnnotationPresent(PlanningEntityCollectionProperty.class)) {
            return true;
        }
        Method getterMethod = ReflectionHelper.getGetterMethod(fieldInstanceClass, field.getName());
        if (getterMethod != null &&
                (getterMethod.isAnnotationPresent(PlanningEntityProperty.class)
                        || getterMethod.isAnnotationPresent(PlanningEntityCollectionProperty.class))) {
            return true;
        }
        return false;
    }

}
