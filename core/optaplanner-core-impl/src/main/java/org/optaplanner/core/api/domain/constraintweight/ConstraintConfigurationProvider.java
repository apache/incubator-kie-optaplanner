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

package org.optaplanner.core.api.domain.constraintweight;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;

/**
 * Specifies that a property (or a field) on a {@link PlanningSolution} class is a {@link ConstraintConfiguration}.
 * This property is automatically a {@link ProblemFactProperty} too, so no need to declare that explicitly.
 * <p>
 * The type of this property (or field) must have a {@link ConstraintConfiguration} annotation.
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface ConstraintConfigurationProvider {

}
