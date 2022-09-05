package org.optaplanner.examples.vehiclerouting.domain;

import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedDepot;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

@XStreamAlias("VrpDepot")
@XStreamInclude({
        TimeWindowedDepot.class
})
public class Depot extends AbstractPersistable {

    protected Location location;

    public Depot() {
    }

    public Depot(long id, Location location) {
        super(id);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName();
    }

}
