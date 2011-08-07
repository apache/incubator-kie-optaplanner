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

package org.drools.planner.api.domain.variable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningValueStrengthWeightFactory;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Specifies that a bean property should be optimized by Drools Planner.
 * <p/>
 * It is specified on a getter of a java bean property of a class with the {@link PlanningEntity} annotation.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface PlanningVariable {

    // TODO Add null
    // boolean nullable() default false;

    /**
     * Allows a collection of planning values for this variable to be sorted by strength.
     * <p/>
     * Do not use together with {@link #strengthWeightFactoryClass()}.
     * @return {@link NullStrengthComparator} when it is null (workaround for annotation limitation)
     */
    public Class<? extends Comparator> strengthComparatorClass() default NullStrengthComparator.class;
    interface NullStrengthComparator extends Comparator {}

    /**
     * Allows a collection of planning values for this variable  to be sorted by strength.
     * <p/>
     * Do not use together with {@link #strengthComparatorClass()}.
     * @return {@link NullStrengthWeightFactory} when it is null (workaround for annotation limitation)
     * @see PlanningValueStrengthWeightFactory
     */
    public Class<? extends PlanningValueStrengthWeightFactory> strengthWeightFactoryClass()
            default NullStrengthWeightFactory.class;
    interface NullStrengthWeightFactory extends PlanningValueStrengthWeightFactory {}

}
