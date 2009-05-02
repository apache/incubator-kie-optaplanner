package org.drools.solver.examples.manners2009.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Job extends AbstractPersistable implements Comparable<Job> {

    private JobType jobType;
    private String name;

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Job other) {
        return new CompareToBuilder()
                .append(jobType, other.jobType)
                .append(name, other.name)
                .append(id, other.id)
                .toComparison();
    }

    @Override
    public String toString() {
        return name + "(" + jobType.getCode() + ")";
    }

}