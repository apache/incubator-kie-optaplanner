/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.api.domain.solution;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.core.solution.Solution;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Specifies that a property on a {@link Solution} is a planning entity.
 * <p/>
 * The planning entity should have the {@link PlanningEntity} annotation.
 * An initialized planning entity will be inserted in the WorkingMemory.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface PlanningEntityProperty {

    // TODO factory for dynamic length entity collections
    // PlanningEntityFactory factory() default Void.class;

}
