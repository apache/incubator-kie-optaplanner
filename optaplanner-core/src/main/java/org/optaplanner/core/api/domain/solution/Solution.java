/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.domain.solution;

import org.optaplanner.core.api.score.Score;

/**
 * A Solution represents a problem and a possible solution of that problem.
 * A possible solution does not need to be optimal or even feasible.
 * A Solution's variables do not even have to be initialized.
 * <p>
 * A Solution is mutable.
 * For scalability reasons, the same Solution instance, called the working solution, is continuously modified.
 * It's cloned to recall the best solution.
 * <p>
 * This annotation described declarative properties of the planning solution.
 * The planning solution class must implement this interface which is needed to get/set state.
 * But the planning solution class must also be annotated with {@link PlanningSolution}
 * describes declarative properties.
 * @param <S> the {@link Score} type used by this use case
 */
public interface Solution<S extends Score> {

}
