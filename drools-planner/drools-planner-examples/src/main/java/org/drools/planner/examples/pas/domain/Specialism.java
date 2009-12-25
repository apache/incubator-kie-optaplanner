package org.drools.planner.examples.pas.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
@XStreamAlias("Specialism")
public class Specialism extends AbstractPersistable implements Comparable<Specialism> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Specialism other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return name;
    }

}