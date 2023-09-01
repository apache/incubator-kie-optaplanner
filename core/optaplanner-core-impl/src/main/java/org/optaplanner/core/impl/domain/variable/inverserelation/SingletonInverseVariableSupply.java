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

package org.optaplanner.core.impl.domain.variable.inverserelation;

import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.impl.domain.variable.supply.Supply;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * Currently only supported for chained variables and {@link PlanningListVariable list variables},
 * which guarantee that no 2 entities use the same planningValue.
 * <p>
 * To get an instance, demand a {@link SingletonInverseVariableDemand} (for a chained variable)
 * or a {@link SingletonListInverseVariableDemand} (for a list variable) from {@link InnerScoreDirector#getSupplyManager()}.
 */
public interface SingletonInverseVariableSupply extends Supply {

    /**
     * If entity1.varA = x then the inverse of x is entity1.
     *
     * @param planningValue never null
     * @return sometimes null, an entity for which the planning variable is the planningValue.
     */
    Object getInverseSingleton(Object planningValue);

}
