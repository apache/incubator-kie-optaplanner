package org.optaplanner.examples.vehiclerouting.domain;

import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.IndexShadowVariable;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.solver.DepotAngleCustomerDifficultyWeightFactory;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@PlanningEntity(difficultyWeightFactoryClass = DepotAngleCustomerDifficultyWeightFactory.class)
@XStreamAlias("VrpCustomer")
@XStreamInclude({
        TimeWindowedCustomer.class
})
public class Customer extends AbstractPersistable {

    protected Location location;
    protected int demand;

    // Shadow variables
    protected Vehicle vehicle;
    protected Integer index;

    public Customer() {
    }

    public Customer(long id, Location location, int demand) {
        super(id);
        this.location = location;
        this.demand = demand;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @InverseRelationShadowVariable(sourceVariableName = "customers")
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @IndexShadowVariable(sourceVariableName = "customers")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public long getDistanceFromPreviousStandstill() {
        if (vehicle == null || index == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (index == 0) {
            return vehicle.getLocation().getDistanceTo(location);
        }
        return vehicle.getCustomers().get(index - 1).getLocation().getDistanceTo(location);
    }

    public Customer getNextCustomer() {
        List<Customer> customers = vehicle.getCustomers();
        if (index == customers.size() - 1) {
            return null;
        }
        return customers.get(index + 1);
    }

    public long getDistanceTo(Vehicle vehicle) {
        return location.getDistanceTo(vehicle.getLocation());
    }

    @Override
    public String toString() {
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName();
    }

}
