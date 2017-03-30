/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.cloudbalancing.optional.move;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.cloudbalancing.domain.CloudBalance;
import org.optaplanner.examples.cloudbalancing.domain.CloudComputer;
import org.optaplanner.examples.cloudbalancing.domain.CloudProcess;

public class CloudComputerChangeMove extends AbstractMove<CloudBalance> {

    private CloudProcess cloudProcess;
    private CloudComputer toCloudComputer;

    public CloudComputerChangeMove(CloudProcess cloudProcess, CloudComputer toCloudComputer) {
        this.cloudProcess = cloudProcess;
        this.toCloudComputer = toCloudComputer;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<CloudBalance> scoreDirector) {
        return !Objects.equals(cloudProcess.getComputer(), toCloudComputer);
    }

    @Override
    public CloudComputerChangeMove createUndoMove(ScoreDirector<CloudBalance> scoreDirector) {
        return new CloudComputerChangeMove(cloudProcess, cloudProcess.getComputer());
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<CloudBalance> scoreDirector) {
        scoreDirector.beforeVariableChanged(cloudProcess, "computer");
        cloudProcess.setComputer(toCloudComputer);
        scoreDirector.afterVariableChanged(cloudProcess, "computer");
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(cloudProcess);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toCloudComputer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CloudComputerChangeMove) {
            CloudComputerChangeMove other = (CloudComputerChangeMove) o;
            return new EqualsBuilder()
                    .append(cloudProcess, other.cloudProcess)
                    .append(toCloudComputer, other.toCloudComputer)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(cloudProcess)
                .append(toCloudComputer)
                .toHashCode();
    }

    @Override
    public String toString() {
        return cloudProcess + " {" + cloudProcess.getComputer() + " -> " + toCloudComputer + "}";
    }

}
