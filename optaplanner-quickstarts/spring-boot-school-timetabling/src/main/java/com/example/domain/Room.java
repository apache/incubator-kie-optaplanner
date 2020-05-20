package com.example.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Room {

    @PlanningId
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    private Room() {
    }

    public Room(String name) {
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
