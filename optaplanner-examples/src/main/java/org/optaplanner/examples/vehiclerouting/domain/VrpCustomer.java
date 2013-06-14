/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.examples.vehiclerouting.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.value.ValueRange;
import org.optaplanner.core.api.domain.value.ValueRangeType;
import org.optaplanner.core.api.domain.value.ValueRanges;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.solver.VrpCustomerDifficultyComparator;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.VrpTimeWindowedCustomer;

@PlanningEntity(difficultyComparatorClass = VrpCustomerDifficultyComparator.class)
@XStreamAlias("VrpCustomer")
@XStreamInclude({
        VrpTimeWindowedCustomer.class
})
public class VrpCustomer extends AbstractPersistable implements VrpStandstill {

    protected VrpLocation location;
    protected int demand;

    // Planning variables: changes during planning, between score calculations.
    protected VrpStandstill previousStandstill;
    protected VrpCustomer nextCustomer;

    public VrpLocation getLocation() {
        return location;
    }

    public void setLocation(VrpLocation location) {
        this.location = location;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @PlanningVariable(chained = true)
    @ValueRanges({
            @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "vehicleList"),
            @ValueRange(type = ValueRangeType.FROM_SOLUTION_PROPERTY, solutionProperty = "customerList",
                    excludeUninitializedPlanningEntity = true)})
    public VrpStandstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(VrpStandstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @PlanningVariable(mappedBy = "previousStandstill")
    public VrpCustomer getNextCustomer() {
        return nextCustomer;
    }

    public void setNextCustomer(VrpCustomer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public VrpVehicle getVehicle() {
        // HACK TODO Invent a system like DependentPlanningVariable or PlanningVariableListener to cope with this
        VrpStandstill firstStandstill = getPreviousStandstill();
        while (firstStandstill instanceof VrpCustomer) {
            if (firstStandstill == this) {
                throw new IllegalStateException("Impossible state"); // fail fast during infinite loop
            }
            firstStandstill = ((VrpCustomer) firstStandstill).getPreviousStandstill();
        }
        return (VrpVehicle) firstStandstill;
    }

    public int getDistanceToPreviousStandstill() {
        if (previousStandstill == null) {
            return 0;
        }
        return getDistanceTo(previousStandstill);
    }

    public int getDistanceTo(VrpStandstill standstill) {
        return location.getDistance(standstill.getLocation());
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionHashCode()
     */
    public boolean solutionEquals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof VrpCustomer) {
            VrpCustomer other = (VrpCustomer) o;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(location, other.location) // TODO performance leak: not needed?
                    .append(previousStandstill, other.previousStandstill) // TODO performance leak: not needed?
                    .isEquals();
        } else {
            return false;
        }
    }

    /**
     * The normal methods {@link #equals(Object)} and {@link #hashCode()} cannot be used because the rule engine already
     * requires them (for performance in their original state).
     * @see #solutionEquals(Object)
     */
    public int solutionHashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(location) // TODO performance leak: not needed?
                .append(previousStandstill) // TODO performance leak: not needed?
                .toHashCode();
    }

    @Override
    public String toString() {
        return location + "(after " + (previousStandstill == null ? "null" : previousStandstill.getLocation()) + ")";
    }

}
