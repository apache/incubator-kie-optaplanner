package org.optaplanner.examples.common.domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public abstract class AbstractPersistableJaxb {

    protected Long id;

    protected AbstractPersistableJaxb() { // For Jackson.
    }

    protected AbstractPersistableJaxb(long id) {
        this.id = id;
    }

    @PlanningId
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

}
