package org.drools.planner.examples.manners2009.domain;

import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Guest extends AbstractPersistable implements Comparable<Guest> {

    private String code;
    private Job job;
    private Gender gender;

    private List<HobbyPractician> hobbyPracticianList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<HobbyPractician> getHobbyPracticianList() {
        return hobbyPracticianList;
    }

    public void setHobbyPracticianList(List<HobbyPractician> hobbyPracticianList) {
        this.hobbyPracticianList = hobbyPracticianList;
    }

    public int compareTo(Guest other) {
        return new CompareToBuilder()
                .append(code, other.code)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return code + "(" + job + "," + gender.getCode() + ")";
    }

}