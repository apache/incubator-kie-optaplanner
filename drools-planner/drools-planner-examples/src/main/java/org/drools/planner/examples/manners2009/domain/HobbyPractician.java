package org.drools.planner.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class HobbyPractician extends AbstractPersistable implements Comparable<HobbyPractician> {

    private Guest guest;
    private Hobby hobby;

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Hobby getHobby() {
        return hobby;
    }

    public void setHobby(Hobby hobby) {
        this.hobby = hobby;
    }

    public int compareTo(HobbyPractician other) {
        return new CompareToBuilder()
                .append(guest, other.guest)
                .append(hobby, other.hobby)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return guest + "-" + hobby;
    }

}