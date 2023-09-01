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

package org.optaplanner.core.impl.domain.variable.listener.support.violation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.shadow.TestdataShadowingChainedSolution;

class ShadowVariablesAssertTest {

    @Test
    void violationMessage() {
        TestdataShadowingChainedAnchor a0 = new TestdataShadowingChainedAnchor("a0");
        TestdataShadowingChainedEntity a1 = new TestdataShadowingChainedEntity("a1", a0);
        a1.setAnchor(a0);
        a0.setNextEntity(a1);
        TestdataShadowingChainedEntity a2 = new TestdataShadowingChainedEntity("a2", a1);
        a2.setAnchor(a0);
        a1.setNextEntity(a2);
        TestdataShadowingChainedEntity a3 = new TestdataShadowingChainedEntity("a3", a2);
        a3.setAnchor(a0);
        a2.setNextEntity(a3);

        TestdataShadowingChainedSolution solution = new TestdataShadowingChainedSolution("solution");
        solution.setChainedAnchorList(Arrays.asList(a0));
        solution.setChainedEntityList(Arrays.asList(a1, a2, a3));

        ShadowVariablesAssert snapshot =
                ShadowVariablesAssert.takeSnapshot(TestdataShadowingChainedSolution.buildSolutionDescriptor(), solution);

        // No change => no violation.
        assertThat(snapshot.createShadowVariablesViolationMessage(10)).isNull();

        // Assume that triggering variable listeners on the working solution changes shadow variables.
        a0.setNextEntity(a3);
        a1.setNextEntity(null);
        a2.setNextEntity(a2);
        a3.setNextEntity(a1);
        a1.setAnchor(null);
        a2.setAnchor(new TestdataShadowingChainedAnchor("x"));
        a3.setAnchor(null);

        assertThat(snapshot.createShadowVariablesViolationMessage(2))
                // FIXME shadow entities should be reported as well (https://issues.redhat.com/browse/PLANNER-2691).
                // .startsWith("    The entity (a0)")
                .containsSubsequence(
                        "corrupted value (a2) changed to uncorrupted value (null)",
                        "... 1 more",
                        "corrupted value (a0) changed to uncorrupted value (x)",
                        "... 1 more");
    }
}
