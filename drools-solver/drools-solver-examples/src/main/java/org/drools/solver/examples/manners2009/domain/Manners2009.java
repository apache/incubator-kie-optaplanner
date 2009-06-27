package org.drools.solver.examples.manners2009.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.EnumSet;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Manners2009 extends AbstractPersistable implements Solution {

    private List<Job> jobList;
    private List<Guest> guestList;
    private List<HobbyPractician> hobbyPracticianList;
    private List<Table> tableList;
    private List<Seat> seatList;

    private List<SeatDesignation> seatDesignationList;

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    public List<Guest> getGuestList() {
        return guestList;
    }

    public void setGuestList(List<Guest> guestList) {
        this.guestList = guestList;
    }

    public List<HobbyPractician> getHobbyPracticianList() {
        return hobbyPracticianList;
    }

    public void setHobbyPracticianList(List<HobbyPractician> hobbyPracticianList) {
        this.hobbyPracticianList = hobbyPracticianList;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public void setTableList(List<Table> tableList) {
        this.tableList = tableList;
    }

    public List<Seat> getSeatList() {
        return seatList;
    }

    public void setSeatList(List<Seat> seatList) {
        this.seatList = seatList;
    }

    public List<SeatDesignation> getSeatDesignationList() {
        return seatDesignationList;
    }

    public void setSeatDesignationList(List<SeatDesignation> seatDesignationList) {
        this.seatDesignationList = seatDesignationList;
    }


    public boolean isInitialized() {
        return (seatDesignationList != null);
    }

    public Collection<? extends Object> getFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(EnumSet.allOf(JobType.class));
        facts.addAll(jobList);
        facts.addAll(guestList);
        facts.addAll(EnumSet.allOf(Hobby.class));
        facts.addAll(hobbyPracticianList);
        facts.addAll(tableList);
        facts.addAll(seatList);
        if (isInitialized()) {
            facts.addAll(seatDesignationList);
        }
        return facts;
    }

    /**
     * Clone will only deep copy the matches
     */
    public Manners2009 cloneSolution() {
        Manners2009 clone = new Manners2009();
        clone.id = id;
        clone.jobList = jobList;
        clone.guestList = guestList;
        clone.hobbyPracticianList = hobbyPracticianList;
        clone.tableList = tableList;
        clone.seatList = seatList;
        List<SeatDesignation> clonedSeatDesignationList = new ArrayList<SeatDesignation>(seatDesignationList.size());
        for (SeatDesignation seatDesignation : seatDesignationList) {
            clonedSeatDesignationList.add(seatDesignation.clone());
        }
        clone.seatDesignationList = clonedSeatDesignationList;
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (id == null || !(o instanceof Manners2009)) {
            return false;
        } else {
            Manners2009 other = (Manners2009) o;
            if (seatDesignationList.size() != other.seatDesignationList.size()) {
                return false;
            }
            for (Iterator<SeatDesignation> it = seatDesignationList.iterator(), otherIt = other.seatDesignationList.iterator(); it.hasNext();) {
                SeatDesignation seatDesignation = it.next();
                SeatDesignation otherSeatDesignation = otherIt.next();
                // Notice: we don't use equals()
                if (!seatDesignation.solutionEquals(otherSeatDesignation)) {
                    return false;
                }
            }
            return true;
        }
    }

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        for (SeatDesignation seatDesignation : seatDesignationList) {
            // Notice: we don't use hashCode()
            hashCodeBuilder.append(seatDesignation.solutionHashCode());
        }
        return hashCodeBuilder.toHashCode();
    }

}