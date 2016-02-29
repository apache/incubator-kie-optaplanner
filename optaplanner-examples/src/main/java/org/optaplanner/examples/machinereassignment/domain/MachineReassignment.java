/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.machinereassignment.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.*;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.buildin.hardsoftlong.HardSoftLongScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.machinereassignment.domain.solver.MrServiceDependency;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

import java.util.ArrayList;
import java.util.List;

@PlanningSolution
@XStreamAlias("MachineReassignment")
public class MachineReassignment extends AbstractPersistable implements Solution<HardSoftLongScore> {

    @PlanningFactProperty
    private MrGlobalPenaltyInfo globalPenaltyInfo;
    @PlanningFactCollectionProperty
    private List<MrResource> resourceList;
    @PlanningFactCollectionProperty
    private List<MrNeighborhood> neighborhoodList;
    @PlanningFactCollectionProperty
    private List<MrLocation> locationList;
    @PlanningFactCollectionProperty
    private List<MrMachine> machineList;
    @PlanningFactCollectionProperty
    private List<MrMachineCapacity> machineCapacityList;
    @PlanningFactCollectionProperty
    private List<MrService> serviceList;
    @PlanningFactCollectionProperty
    private List<MrProcess> processList;
    @PlanningFactCollectionProperty
    private List<MrBalancePenalty> balancePenaltyList;

    private List<MrProcessAssignment> processAssignmentList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftLongScoreDefinition.class})
    private HardSoftLongScore score;

    public MrGlobalPenaltyInfo getGlobalPenaltyInfo() {
        return globalPenaltyInfo;
    }

    public void setGlobalPenaltyInfo(MrGlobalPenaltyInfo globalPenaltyInfo) {
        this.globalPenaltyInfo = globalPenaltyInfo;
    }

    public List<MrResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<MrResource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<MrNeighborhood> getNeighborhoodList() {
        return neighborhoodList;
    }

    public void setNeighborhoodList(List<MrNeighborhood> neighborhoodList) {
        this.neighborhoodList = neighborhoodList;
    }

    public List<MrLocation> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<MrLocation> locationList) {
        this.locationList = locationList;
    }

    @ValueRangeProvider(id = "machineRange")
    public List<MrMachine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<MrMachine> machineList) {
        this.machineList = machineList;
    }

    public List<MrMachineCapacity> getMachineCapacityList() {
        return machineCapacityList;
    }

    public void setMachineCapacityList(List<MrMachineCapacity> machineCapacityList) {
        this.machineCapacityList = machineCapacityList;
    }

    public List<MrService> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<MrService> serviceList) {
        this.serviceList = serviceList;
    }

    public List<MrProcess> getProcessList() {
        return processList;
    }

    public void setProcessList(List<MrProcess> processList) {
        this.processList = processList;
    }

    public List<MrBalancePenalty> getBalancePenaltyList() {
        return balancePenaltyList;
    }

    public void setBalancePenaltyList(List<MrBalancePenalty> balancePenaltyList) {
        this.balancePenaltyList = balancePenaltyList;
    }

    @PlanningEntityCollectionProperty
    public List<MrProcessAssignment> getProcessAssignmentList() {
        return processAssignmentList;
    }

    public void setProcessAssignmentList(List<MrProcessAssignment> processAssignmentList) {
        this.processAssignmentList = processAssignmentList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @PlanningFactCollectionProperty
    private List<MrServiceDependency> getServiceDependencyList() {
        List<MrServiceDependency> serviceDependencyList = new ArrayList<MrServiceDependency>(serviceList.size() * 5);
        for (MrService service : serviceList) {
            for (MrService toService : service.getToDependencyServiceList()) {
                MrServiceDependency serviceDependency = new MrServiceDependency();
                serviceDependency.setFromService(service);
                serviceDependency.setToService(toService);
                serviceDependencyList.add(serviceDependency);
            }
        }
        return serviceDependencyList;
    }

}
