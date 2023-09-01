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

class BreadthFirstNodeComparatorTest extends AbstractNodeComparatorTest {

    @Test
    void compare() {
        BreadthFirstNodeComparator comparator = new BreadthFirstNodeComparator(true);
        assertScoreCompareToOrder(comparator,
                buildNode(2, "-110", 5, 51),
                buildNode(2, "-110", 5, 50),
                buildNode(2, "-90", 7, 41),
                buildNode(2, "-90", 5, 40),
                buildNode(1, "-110", 7, 61),
                buildNode(1, "-110", 5, 60),
                buildNode(1, "-90", 7, 71),
                buildNode(1, "-90", 5, 70),
                buildNode(1, "-85", 5, 60),
                buildNode(1, "-1init/-80", 5, 60));
    }

}
