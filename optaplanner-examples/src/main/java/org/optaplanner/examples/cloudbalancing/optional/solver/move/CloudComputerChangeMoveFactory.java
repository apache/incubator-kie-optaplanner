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

package org.optaplanner.examples.cloudbalancing.optional.solver.move;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudComputer;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;

public class CloudComputerChangeMoveFactory implements MoveListFactory<CloudBalance> {

    @Override
    public List<CloudComputerChangeMove> createMoveList(CloudBalance cloudBalance) {
        List<CloudComputerChangeMove> moveList = new ArrayList<>();
        List<CloudComputer> cloudComputerList = cloudBalance.getComputerList();
        for (CloudProcess cloudProcess : cloudBalance.getProcessList()) {
            for (CloudComputer cloudComputer : cloudComputerList) {
                moveList.add(new CloudComputerChangeMove(cloudProcess, cloudComputer));
            }
        }
        return moveList;
    }

}
