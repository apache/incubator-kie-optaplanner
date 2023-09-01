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

package org.optaplanner.core.impl.localsearch.decider.forager;

import java.util.Objects;

import org.optaplanner.core.config.localsearch.decider.forager.FinalistPodiumType;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;

public class LocalSearchForagerFactory<Solution_> {

    public static <Solution_> LocalSearchForagerFactory<Solution_> create(LocalSearchForagerConfig foragerConfig) {
        return new LocalSearchForagerFactory<>(foragerConfig);
    }

    private final LocalSearchForagerConfig foragerConfig;

    public LocalSearchForagerFactory(LocalSearchForagerConfig foragerConfig) {
        this.foragerConfig = foragerConfig;
    }

    public LocalSearchForager<Solution_> buildForager() {
        LocalSearchPickEarlyType pickEarlyType_ =
                Objects.requireNonNullElse(foragerConfig.getPickEarlyType(), LocalSearchPickEarlyType.NEVER);
        int acceptedCountLimit_ = Objects.requireNonNullElse(foragerConfig.getAcceptedCountLimit(), Integer.MAX_VALUE);
        FinalistPodiumType finalistPodiumType_ =
                Objects.requireNonNullElse(foragerConfig.getFinalistPodiumType(), FinalistPodiumType.HIGHEST_SCORE);
        // Breaking ties randomly leads to better results statistically
        boolean breakTieRandomly_ = Objects.requireNonNullElse(foragerConfig.getBreakTieRandomly(), true);
        return new AcceptedLocalSearchForager<>(finalistPodiumType_.buildFinalistPodium(), pickEarlyType_,
                acceptedCountLimit_, breakTieRandomly_);
    }
}
