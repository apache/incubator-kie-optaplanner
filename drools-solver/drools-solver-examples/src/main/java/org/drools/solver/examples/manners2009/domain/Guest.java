package org.drools.solver.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Guest extends AbstractPersistable implements Comparable<Guest> {

    private String code;
    private Job job;
    private Gender gender;

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