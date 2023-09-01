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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.optaplanner.core.api.domain.solution.PlanningSolution;

/**
 * Allows end users to change the constraint weights, by not hard coding them.
 * This annotation specifies that the class holds a number of {@link ConstraintWeight} annotated members.
 * That class must also have a {@link ConstraintWeight weight} for each of the constraints.
 * <p>
 * A {@link PlanningSolution} has at most one field or property annotated with {@link ConstraintConfigurationProvider}
 * with returns a type of the {@link ConstraintConfiguration} annotated class.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ConstraintConfiguration {

    /**
     * The namespace of the constraints.
     * <p>
     * This is the default for every {@link ConstraintWeight#constraintPackage()} in the annotated class.
     *
     * @return defaults to the annotated class's package.
     */
    String constraintPackage() default "";

}
