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

package org.optaplanner.core.impl.localsearch.decider.acceptor.tabu.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

class FixedTabuSizeStrategyTest {

    @Test
    void tabuSize() {
        LocalSearchStepScope stepScope = mock(LocalSearchStepScope.class);
        assertThat(new FixedTabuSizeStrategy(5).determineTabuSize(stepScope)).isEqualTo(5);
        assertThat(new FixedTabuSizeStrategy(17).determineTabuSize(stepScope)).isEqualTo(17);
    }

}
