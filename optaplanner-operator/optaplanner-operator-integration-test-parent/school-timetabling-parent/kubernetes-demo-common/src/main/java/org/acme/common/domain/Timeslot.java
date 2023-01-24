package org.acme.common.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.persistence.Entity;

@Entity
public class Timeslot extends AbstractPersistable {

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    // No-arg constructor required for Hibernate
    public Timeslot() {
    }

    public Timeslot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timeslot(long problemId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        super(problemId);
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timeslot(long id, DayOfWeek dayOfWeek, LocalTime startTime) {
        super(id, SINGLE_PROBLEM_ID);
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = startTime.plusHours(1);
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

}
