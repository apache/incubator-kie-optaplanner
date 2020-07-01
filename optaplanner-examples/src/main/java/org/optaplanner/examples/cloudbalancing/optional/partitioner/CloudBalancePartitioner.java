/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.cloudbalancing.optional.partitioner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudComputer;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;

public class CloudBalancePartitioner implements SolutionPartitioner<CloudBalance> {

    private int partCount = 4;
    private int minimumProcessListSize = 25;

    @SuppressWarnings("unused")
    public void setPartCount(int partCount) {
        this.partCount = partCount;
    }

    @SuppressWarnings("unused")
    public void setMinimumProcessListSize(int minimumProcessListSize) {
        this.minimumProcessListSize = minimumProcessListSize;
    }

    @Override
    public List<CloudBalance> splitWorkingSolution(ScoreDirector<CloudBalance> scoreDirector, Integer runnablePartThreadLimit) {
        CloudBalance originalSolution = scoreDirector.getWorkingSolution();
        List<CloudComputer> originalComputerList = originalSolution.getComputerList();
        List<CloudProcess> originalProcessList = originalSolution.getProcessList();
        int partCount = this.partCount;
        if (originalProcessList.size() / partCount < minimumProcessListSize) {
            partCount = originalProcessList.size() / minimumProcessListSize;
        }
        List<CloudBalance> partList = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            CloudBalance partSolution = new CloudBalance(originalSolution.getId(),
                    new ArrayList<>(originalComputerList.size() / partCount + 1),
                    new ArrayList<>(originalProcessList.size() / partCount + 1));
            partList.add(partSolution);
        }

        int partIndex = 0;
        Map<Long, Pair<Integer, CloudComputer>> idToPartIndexAndComputerMap = new HashMap<>(originalComputerList.size());
        for (CloudComputer originalComputer : originalComputerList) {
            CloudBalance part = partList.get(partIndex);
            CloudComputer computer = new CloudComputer(
                    originalComputer.getId(),
                    originalComputer.getCpuPower(), originalComputer.getMemory(),
                    originalComputer.getNetworkBandwidth(), originalComputer.getCost());
            part.getComputerList().add(computer);
            idToPartIndexAndComputerMap.put(computer.getId(), Pair.of(partIndex, computer));
            partIndex = (partIndex + 1) % partList.size();
        }

        partIndex = 0;
        for (CloudProcess originalProcess : originalProcessList) {
            CloudBalance part = partList.get(partIndex);
            CloudProcess process = new CloudProcess(
                    originalProcess.getId(),
                    originalProcess.getRequiredCpuPower(), originalProcess.getRequiredMemory(),
                    originalProcess.getRequiredNetworkBandwidth());
            part.getProcessList().add(process);
            if (originalProcess.getComputer() != null) {
                Pair<Integer, CloudComputer> partIndexAndComputer = idToPartIndexAndComputerMap.get(
                        originalProcess.getComputer().getId());
                if (partIndexAndComputer == null) {
                    throw new IllegalStateException("The initialized process (" + originalProcess
                            + ") has a computer (" + originalProcess.getComputer()
                            + ") which doesn't exist in the originalSolution (" + originalSolution + ").");
                }
                if (partIndex != partIndexAndComputer.getLeft().intValue()) {
                    throw new IllegalStateException("The initialized process (" + originalProcess
                            + ") with partIndex (" + partIndex
                            + ") has a computer (" + originalProcess.getComputer()
                            + ") which belongs to another partIndex (" + partIndexAndComputer.getLeft() + ").");
                }
                process.setComputer(partIndexAndComputer.getRight());
            }
            partIndex = (partIndex + 1) % partList.size();
        }
        return partList;
    }

}
