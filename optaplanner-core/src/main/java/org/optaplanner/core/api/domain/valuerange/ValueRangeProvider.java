/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.domain.valuerange;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.solver.SolverFactory;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Provides the planning values that can be used for a {@link PlanningVariable}.
 * <p>
 * This is specified on a getter of a java bean property (or directly on a field)
 * which returns a {@link Collection} or {@link ValueRange}.
 * A {@link Collection} is implicitly converted to a {@link ValueRange}.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValueRangeProvider {

    /**
     * Used by {@link PlanningVariable#valueRangeProviderRefs()}
     * to map a {@link PlanningVariable} to a {@link ValueRangeProvider}.
     * @return never null, must be unique across a {@link SolverFactory}
     */
    String id();

}
