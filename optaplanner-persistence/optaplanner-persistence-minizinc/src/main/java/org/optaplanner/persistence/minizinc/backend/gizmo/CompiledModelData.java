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

package org.optaplanner.persistence.minizinc.backend.gizmo;

import java.util.List;

import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class CompiledModelData {
    final Class<? extends ConstraintProvider> constraintProviderClass;
    final Class<?> planningSolutionClass;
    final List<Class<?>> planningEntityClasses;
    final ClassLoader classLoader;
    final Object compiledProblem;

    public CompiledModelData(Class<? extends ConstraintProvider> constraintProviderClass, Class<?> planningSolutionClass,
            List<Class<?>> planningEntityClasses, ClassLoader classLoader, Object compiledProblem) {
        this.constraintProviderClass = constraintProviderClass;
        this.planningSolutionClass = planningSolutionClass;
        this.planningEntityClasses = planningEntityClasses;
        this.classLoader = classLoader;
        this.compiledProblem = compiledProblem;
    }

    public Class<? extends ConstraintProvider> getConstraintProviderClass() {
        return constraintProviderClass;
    }

    public Class<?> getPlanningSolutionClass() {
        return planningSolutionClass;
    }

    public List<Class<?>> getPlanningEntityClasses() {
        return planningEntityClasses;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Object getCompiledProblem() {
        return compiledProblem;
    }
}
