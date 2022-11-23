package org.optaplanner.examples.pas.domain;

import org.optaplanner.examples.common.domain.AbstractPersistableJackson;
import org.optaplanner.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import org.optaplanner.examples.common.swingui.components.Labeled;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Night extends AbstractPersistableJackson implements Labeled {

    private int index;

    public Night() { // For Jackson.
    }

    public Night(long id, int index) {
        super(id);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getLabel() {
        return (index + 1) + "-JAN";
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

}
