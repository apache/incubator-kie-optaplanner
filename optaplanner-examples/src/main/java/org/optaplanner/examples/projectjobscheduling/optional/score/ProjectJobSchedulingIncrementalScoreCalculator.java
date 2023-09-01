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

package org.optaplanner.examples.projectjobscheduling.optional.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.calculator.IncrementalScoreCalculator;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.JobType;
import org.optaplanner.examples.projectjobscheduling.domain.Project;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;
import org.optaplanner.examples.projectjobscheduling.optional.score.common.NonrenewableResourceCapacityTracker;
import org.optaplanner.examples.projectjobscheduling.optional.score.common.RenewableResourceCapacityTracker;
import org.optaplanner.examples.projectjobscheduling.optional.score.common.ResourceCapacityTracker;

public class ProjectJobSchedulingIncrementalScoreCalculator
        implements IncrementalScoreCalculator<Schedule, HardMediumSoftScore> {

    private Map<Resource, ResourceCapacityTracker> resourceCapacityTrackerMap;
    private Map<Project, Integer> projectEndDateMap;
    private int maximumProjectEndDate;

    private int hardScore;
    private int mediumScore;
    private int softScore;

    @Override
    public void resetWorkingSolution(Schedule schedule) {
        List<Resource> resourceList = schedule.getResourceList();
        resourceCapacityTrackerMap = new HashMap<>(resourceList.size());
        for (Resource resource : resourceList) {
            resourceCapacityTrackerMap.put(resource, resource.isRenewable()
                    ? new RenewableResourceCapacityTracker(resource)
                    : new NonrenewableResourceCapacityTracker(resource));
        }
        List<Project> projectList = schedule.getProjectList();
        projectEndDateMap = new HashMap<>(projectList.size());
        maximumProjectEndDate = 0;
        hardScore = 0;
        mediumScore = 0;
        softScore = 0;
        int minimumReleaseDate = Integer.MAX_VALUE;
        for (Project p : projectList) {
            minimumReleaseDate = Math.min(p.getReleaseDate(), minimumReleaseDate);
        }
        softScore += minimumReleaseDate;
        for (Allocation allocation : schedule.getAllocationList()) {
            insert(allocation);
        }
    }

    @Override
    public void beforeEntityAdded(Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(Object entity) {
        insert((Allocation) entity);
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        retract((Allocation) entity);
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        insert((Allocation) entity);
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        retract((Allocation) entity);
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insert(Allocation allocation) {
        // Job precedence is built-in
        // Resource capacity
        ExecutionMode executionMode = allocation.getExecutionMode();
        if (executionMode != null && allocation.getJob().getJobType() == JobType.STANDARD) {
            for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
                ResourceCapacityTracker tracker = resourceCapacityTrackerMap.get(
                        resourceRequirement.getResource());
                hardScore -= tracker.getHardScore();
                tracker.insert(resourceRequirement, allocation);
                hardScore += tracker.getHardScore();
            }
        }
        // Total project delay and total make span
        if (allocation.getJob().getJobType() == JobType.SINK) {
            Integer endDate = allocation.getEndDate();
            if (endDate != null) {
                Project project = allocation.getProject();
                projectEndDateMap.put(project, endDate);
                // Total project delay
                mediumScore -= endDate - project.getCriticalPathEndDate();
                // Total make span
                if (endDate > maximumProjectEndDate) {
                    softScore -= endDate - maximumProjectEndDate;
                    maximumProjectEndDate = endDate;
                }
            }
        }
    }

    private void retract(Allocation allocation) {
        // Job precedence is built-in
        // Resource capacity
        ExecutionMode executionMode = allocation.getExecutionMode();
        if (executionMode != null && allocation.getJob().getJobType() == JobType.STANDARD) {
            for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
                ResourceCapacityTracker tracker = resourceCapacityTrackerMap.get(
                        resourceRequirement.getResource());
                hardScore -= tracker.getHardScore();
                tracker.retract(resourceRequirement, allocation);
                hardScore += tracker.getHardScore();
            }
        }
        // Total project delay and total make span
        if (allocation.getJob().getJobType() == JobType.SINK) {
            Integer endDate = allocation.getEndDate();
            if (endDate != null) {
                Project project = allocation.getProject();
                projectEndDateMap.remove(project);
                // Total project delay
                mediumScore += endDate - project.getCriticalPathEndDate();
                // Total make span
                if (endDate == maximumProjectEndDate) {
                    updateMaximumProjectEndDate();
                    softScore += endDate - maximumProjectEndDate;
                }
            }
        }
    }

    private void updateMaximumProjectEndDate() {
        int maximum = 0;
        for (Integer endDate : projectEndDateMap.values()) {
            if (endDate > maximum) {
                maximum = endDate;
            }
        }
        maximumProjectEndDate = maximum;
    }

    @Override
    public HardMediumSoftScore calculateScore() {
        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }

}
