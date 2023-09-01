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

package org.optaplanner.core.config.localsearch.decider.acceptor;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum AcceptorType {
    HILL_CLIMBING,
    ENTITY_TABU,
    VALUE_TABU,
    MOVE_TABU,
    UNDO_MOVE_TABU,
    SIMULATED_ANNEALING,
    LATE_ACCEPTANCE,
    GREAT_DELUGE,
    STEP_COUNTING_HILL_CLIMBING
}
