/*
 * Copyright 2010 JBoss Inc
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

package org.optaplanner.persistence.xstream.score.bendable.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.buildin.bendable.BendableScoreDefinition;
import org.optaplanner.persistence.xstream.XStreamScoreConverter;

@PlanningSolution
@XStreamAlias("CloudBalance")
public class CloudBalance implements Solution<BendableScore> {

    private List<CloudComputer> computerList;

    private List<CloudProcess> processList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {BendableScoreDefinition.class})
    private BendableScore score;

    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public List<CloudComputer> getComputerList() {
        return computerList;
    }

    public void setComputerList(List<CloudComputer> computerList) {
        this.computerList = computerList;
    }

    @PlanningEntityCollectionProperty
    public List<CloudProcess> getProcessList() {
        return processList;
    }

    public void setProcessList(List<CloudProcess> processList) {
        this.processList = processList;
    }

    @Override
    public BendableScore getScore() {
        return score;
    }

    @Override
    public void setScore(BendableScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(computerList);
        // Do not add the planning entity's (processList) because that will be done automatically
        return facts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof CloudBalance)) {
            return false;
        } else {
            CloudBalance other = (CloudBalance) o;
            if (processList.size() != other.processList.size()) {
                return false;
            }
            for (Iterator<CloudProcess> it = processList.iterator(), otherIt = other.processList.iterator(); it.hasNext();) {
                CloudProcess process = it.next();
                CloudProcess otherProcess = otherIt.next();
                // Notice: we don't use equals()
                if (!process.solutionEquals(otherProcess)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (CloudProcess process : processList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(process.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

    public List<CloudProcess> getUnassignedProcesses() {
        List<CloudProcess> unassigned = new ArrayList<CloudProcess>();
        for(CloudProcess proc : getProcessList()) {
            if(proc.getComputer() == null) {
                unassigned.add(proc);
            }
        }
        
        return unassigned;
    }
    
    @Override
    public String toString() {
        return processList.toString() + " : " + getScore().toString();
    }

}
