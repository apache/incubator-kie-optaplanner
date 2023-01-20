package org.acme.common.domain;

import javax.persistence.Entity;

@Entity
public class Room extends AbstractPersistable {

    private String name;

    // No-arg constructor required for Hibernate
    public Room() {
    }

    public Room(String name) {
        this.name = name.trim();
    }

    public Room(long id, String name) {
        super(id, SINGLE_PROBLEM_ID);
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

}
