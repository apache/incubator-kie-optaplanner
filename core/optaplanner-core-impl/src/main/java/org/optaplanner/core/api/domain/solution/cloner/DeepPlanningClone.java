/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.api.domain.solution.cloner;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;

/**
 * Marks a problem fact class as being required to be deep planning cloned.
 * Not needed for a {@link PlanningSolution} or {@link PlanningEntity} because those are automatically deep cloned.
 * <p>
 * It can also mark a property (getter for a field) as being required to be deep planning cloned.
 * This is especially useful for {@link Collection} (or {@link Map}) properties.
 * Not needed for a {@link Collection} (or {@link Map}) property with a generic type of {@link PlanningEntity}
 * or a class with a DeepPlanningClone annotation, because those are automatically deep cloned.
 * Note: If it annotates a property (getter method for a field) returning {@link Collection} (or {@link Map}),
 * it clones the {@link Collection} (or {@link Map}),
 * but its elements (or keys and values) are only cloned if they are of a type that needs to be planning cloned.
 * <p>
 * This annotation is ignored if a custom {@link SolutionCloner} is set with {@link PlanningSolution#solutionCloner()}.
 */
@Target({ TYPE, METHOD, FIELD })
@Inherited
@Retention(RUNTIME)
public @interface DeepPlanningClone {

}
