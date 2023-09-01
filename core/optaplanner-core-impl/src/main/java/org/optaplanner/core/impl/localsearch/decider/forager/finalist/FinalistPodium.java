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

package org.optaplanner.core.impl.localsearch.decider.forager.finalist;

import java.util.List;

import org.optaplanner.core.impl.localsearch.decider.forager.LocalSearchForager;
import org.optaplanner.core.impl.localsearch.event.LocalSearchPhaseLifecycleListener;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

/**
 * A podium gathers the finalists (the {@link LocalSearchMoveScope}s which might win) and picks the winner.
 *
 * @see AbstractFinalistPodium
 * @see HighestScoreFinalistPodium
 */
public interface FinalistPodium<Solution_> extends LocalSearchPhaseLifecycleListener<Solution_> {

    /**
     * See {@link LocalSearchForager#addMove(LocalSearchMoveScope)}.
     *
     * @param moveScope never null
     */
    void addMove(LocalSearchMoveScope<Solution_> moveScope);

    /**
     *
     * @return never null, sometimes empty
     */
    List<LocalSearchMoveScope<Solution_>> getFinalistList();

}
