/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.domain.variable;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;

/**
 * Specifies that a bean property (or a field) of a {@link List} type should be optimized by the optimization
 * algorithms. Unlike {@link PlanningVariable}, the {@link PlanningCollectionVariable} tells solver to change
 * elements inside the list variable instead of changing the list reference.
 * <p>
 * It is specified on a getter of a java bean property (or directly on a field) of a {@link PlanningEntity} class.
 * <h3>Disjoint lists</h3>
 * <p>
 * The type of the {@link PlanningCollectionVariable} annotated bean property (or field) must be {@link List}.
 * Furthermore, the current implementation works under the assumption that the list variables of all entity instances
 * are "disjoint lists":
 * <ul>
 * <li><strong>List</strong> means that the order of elements inside a collection planning variable is significant.</li>
 * <li><strong>Disjoint</strong> means that any given pair of entities have no common elements in their list variables.
 * In other words, each element from the list variable's value range appears in exactly one entity's list variable.</li>
 * </ul>
 *
 * <p>
 * Therefore, we refer to such a planning variable as a <strong>list variable</strong>.
 * <p>
 * This makes sense for common use cases, for example the Vehicle Routing Problem or Task Assigning. In both cases
 * the <em>order</em> in which customers are visited and tasks are being worked on matters. Also, each customer
 * must be visited <em>once</em> and each task must be completed by <em>exactly one</em> employee.
 * <p>
 * <strong>Overconstrained planning is currently not supported for list variables.</strong>
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface PlanningCollectionVariable {
    String[] valueRangeProviderRefs() default {};

    // TODO value strength
}
