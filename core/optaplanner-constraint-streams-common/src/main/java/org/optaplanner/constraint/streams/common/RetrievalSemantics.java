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

package org.optaplanner.constraint.streams.common;

/**
 * Determines the behavior of joins and conditional propagation
 * based on whether they are coming off of a constraint stream started by
 * either {@link org.optaplanner.core.api.score.stream.ConstraintFactory#from(Class)}
 * or {@link org.optaplanner.core.api.score.stream.ConstraintFactory#forEach(Class)}
 * family of methods.
 *
 * <p>
 * For classes which are not planning entities, all of their instances are always retrieved.
 * For classes which are planning entities,
 * the difference in behavior depends on whether they use nullable planning variables.
 * (See {@link org.optaplanner.core.api.domain.variable.PlanningVariable}.)
 */
public enum RetrievalSemantics {

    /**
     * Joins do not include entities with null planning variables,
     * unless specifically requested by join(forEachIncludingNullVars(...)).
     * Conditional propagation does not include null planning variables,
     * unless specifically requested using a *IncludingNullVars() method overload.
     *
     * <p>
     * Applies when the stream comes off of a {@link org.optaplanner.core.api.score.stream.ConstraintFactory#forEach(Class)}
     * family of methods.
     */
    STANDARD,
    /**
     * Joins include entities with null planning variables if these variables are nullable.
     * Conditional propagation always includes entities with null planning variables,
     * regardless of whether their planning variables are nullable.
     *
     * <p>
     * Applies when the stream comes off of a {@link org.optaplanner.core.api.score.stream.ConstraintFactory#from(Class)}
     * family of methods.
     *
     * @deprecated this semantics is deprecated and kept around for backward compatibility reasons. It will be removed in 9.0
     *             together with the from() family of methods, along with this entire enum.
     */
    @Deprecated(forRemoval = true)
    LEGACY
}
