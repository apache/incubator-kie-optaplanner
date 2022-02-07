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
package org.optaplanner.core.impl.partitionedsearch;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

public class TestdataSolutionPartitioner implements SolutionPartitioner<TestdataSolution> {

    /**
     * {@link PartitionedSearchPhaseConfig#solutionPartitionerCustomProperties Custom property}.
     */
    private int partSize = 1;

    public void setPartSize(int partSize) {
        this.partSize = partSize;
    }

    @Override
    public List<TestdataSolution> splitWorkingSolution(ScoreDirector<TestdataSolution> scoreDirector,
            Integer runnablePartThreadLimit) {
        TestdataSolution workingSolution = scoreDirector.getWorkingSolution();
        List<TestdataEntity> allEntities = workingSolution.getEntityList();
        if (allEntities.size() % partSize > 0) {
            throw new IllegalStateException("This partitioner can only make equally sized partitions."
                    + " This is impossible because number of allEntities (" + allEntities.size()
                    + ") is not divisible by partSize (" + partSize + ").");
        }
        List<TestdataSolution> partitions = new ArrayList<>();
        for (int i = 0; i < allEntities.size() / partSize; i++) {
            List<TestdataEntity> partitionEntitites = new ArrayList<>(allEntities.subList(i * partSize, (i + 1) * partSize));
            TestdataSolution partition = new TestdataSolution();
            partition.setEntityList(partitionEntitites);
            partition.setValueList(workingSolution.getValueList());
            partitions.add(partition);
        }
        return partitions;
    }

}
