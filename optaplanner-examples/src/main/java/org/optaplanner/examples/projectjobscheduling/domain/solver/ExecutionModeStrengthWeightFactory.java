/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.projectjobscheduling.domain.solver;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class ExecutionModeStrengthWeightFactory implements SelectionSorterWeightFactory<Schedule, ExecutionMode> {

    public Comparable createSorterWeight(Schedule schedule, ExecutionMode executionMode) {
        Map<Resource, Integer> requirementTotalMap = new HashMap<Resource, Integer>(
                executionMode.getResourceRequirementList().size());
        for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
            requirementTotalMap.put(resourceRequirement.getResource(), 0);
        }
        for (ResourceRequirement resourceRequirement : schedule.getResourceRequirementList()) {
            Resource resource = resourceRequirement.getResource();
            Integer total = requirementTotalMap.get(resource);
            if (total != null) {
                total += resourceRequirement.getRequirement();
                requirementTotalMap.put(resource, total);
            }
        }
        double requirementDesirability = 0.0;
        for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
            Resource resource = resourceRequirement.getResource();
            int total = requirementTotalMap.get(resource);
            if (total > resource.getCapacity()) {
                requirementDesirability += (double) (total - resource.getCapacity())
                        * (double) resourceRequirement.getRequirement()
                        * (resource.isRenewable() ? 1.0 : 100.0);
             }
        }
        return new ExecutionModeStrengthWeight(executionMode, requirementDesirability);
    }

    public static class ExecutionModeStrengthWeight implements Comparable<ExecutionModeStrengthWeight> {

        private final ExecutionMode executionMode;
        private final double requirementDesirability;

        public ExecutionModeStrengthWeight(ExecutionMode executionMode, double requirementDesirability) {
            this.executionMode = executionMode;
            this.requirementDesirability = requirementDesirability;
        }

        public int compareTo(ExecutionModeStrengthWeight other) {
            return new CompareToBuilder()
                    // The less requirementsWeight, the less desirable resources are used
                    .append(requirementDesirability, other.requirementDesirability)
                    .append(executionMode.getId(), other.executionMode.getId())
                    .toComparison();
        }

    }

}
