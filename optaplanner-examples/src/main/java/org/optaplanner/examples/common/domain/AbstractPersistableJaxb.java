package org.optaplanner.examples.common.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPersistableJaxb {


    protected Long id;

    protected AbstractPersistableJaxb() { // For JAXB.
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
