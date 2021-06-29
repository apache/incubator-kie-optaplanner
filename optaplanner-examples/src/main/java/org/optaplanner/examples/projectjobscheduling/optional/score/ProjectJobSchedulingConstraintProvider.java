/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.projectjobscheduling.optional.score;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.JobType;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;

public class ProjectJobSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                nonRenewableResourceCapacity(constraintFactory),
                renewableResourceCapacity(constraintFactory),
                totalProjectDelay(constraintFactory),
                totalMakespan(constraintFactory)
        };
    }

    protected Constraint nonRenewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ResourceRequirement.class)
                .filter(resource -> !resource.isResourceRenewable())
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::getExecutionMode, Allocation::getExecutionMode))
                .groupBy((requirement, allocation) -> requirement.getResource(),
                        ConstraintCollectors.sum((requirement, allocation) -> requirement.getRequirement()))
                .filter((resource, requirements) -> requirements > resource.getCapacity())
                .penalize("Non-renewable resource capacity",
                        HardMediumSoftScore.ofHard(1),
                        (resource, requirements) -> requirements - resource.getCapacity());
    }

    protected Constraint renewableResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ResourceRequirement.class)
                .filter(ResourceRequirement::isResourceRenewable)
                .join(Allocation.class,
                        Joiners.equal(ResourceRequirement::getExecutionMode, Allocation::getExecutionMode))
                .flattenLast(a -> IntStream.range(a.getStartDate(), a.getEndDate())
                        .boxed()
                        .collect(Collectors.toList()))
                .groupBy((resourceReq, date) -> resourceReq.getResource(),
                        (resourceReq, date) -> date,
                        ConstraintCollectors.sum((resourceReq, date) -> resourceReq.getRequirement()))
                .filter((resourceReq, date, totalRequirement) -> totalRequirement > resourceReq.getCapacity())
                .penalize("Renewable resource capacity",
                        HardMediumSoftScore.ofHard(1),
                        (resourceReq, date, totalRequirement) -> totalRequirement - resourceReq.getCapacity());
    }

    protected Constraint totalProjectDelay(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Allocation.class)
                .filter(allocation -> allocation.getEndDate() != null)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .impact("Total project delay",
                        HardMediumSoftScore.ofMedium(1),
                        allocation -> allocation.getProjectCriticalPathEndDate() - allocation.getEndDate());
    }

    protected Constraint totalMakespan(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Allocation.class)
                .filter(allocation -> allocation.getEndDate() != null)
                .filter(allocation -> allocation.getJobType() == JobType.SINK)
                .groupBy(ConstraintCollectors.max(Allocation::getEndDate))
                .penalize("Total makespan",
                        HardMediumSoftScore.ofSoft(1),
                        maxEndDate -> maxEndDate);
    }

}
