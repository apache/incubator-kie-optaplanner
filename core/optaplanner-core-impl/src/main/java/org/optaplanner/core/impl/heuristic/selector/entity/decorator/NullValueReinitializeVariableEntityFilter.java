/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.entity.decorator;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;

/**
 * Behaves as if it was a UninitializedVariableEntityFilter, except when the variable is
 * {@link PlanningVariable#nullable()}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class NullValueReinitializeVariableEntityFilter<Solution_> implements SelectionFilter<Solution_, Object> {

    private final GenuineVariableDescriptor<Solution_> variableDescriptor;

    public NullValueReinitializeVariableEntityFilter(GenuineVariableDescriptor<Solution_> variableDescriptor) {
        this.variableDescriptor = variableDescriptor;
    }

    @Override
    public boolean accept(ScoreDirector<Solution_> scoreDirector, Object entity) {
        // Do not use variableDescriptor.isInitialized() because if nullable it must also accept it
        Object value = variableDescriptor.getValue(entity);
        return value == null;
    }

}
