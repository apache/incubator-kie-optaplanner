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

package org.optaplanner.core.impl.exhaustivesearch.node.comparator;

import org.junit.jupiter.api.Test;

class OptimisticBoundFirstNodeComparatorTest extends AbstractNodeComparatorTest {

    @Test
    void compare() {
        OptimisticBoundFirstNodeComparator comparator = new OptimisticBoundFirstNodeComparator(true);
        assertScoreCompareToOrder(comparator,
                buildNode(1, "-300", 5, 41),
                buildNode(1, "-300", 5, 40),
                buildNode(1, "-10init/-200", 5, 40),
                buildNode(1, "-110", 5, 40),
                buildNode(1, "-110", 7, 40),
                buildNode(2, "-110", 5, 40),
                buildNode(2, "-110", 7, 40),
                buildNode(1, "-90", 5, 40),
                buildNode(1, "-90", 7, 40),
                buildNode(2, "-90", 5, 40),
                buildNode(2, "-90", 7, 40),
                buildNode(1, "-95", 0, 5, 40),
                buildNode(2, "-95", 0, 5, 40),
                buildNode(2, "-95", 0, 7, 40),
                buildNode(1, "-11", 1, 5, 40),
                buildNode(1, "-1init/-10", 1, 5, 40));
    }

}
