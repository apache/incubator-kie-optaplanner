package org.acme.common.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@MappedSuperclass
public class AbstractPersistable {

    public static final String TENANT_FIELD = "problemId";
    public static final long SINGLE_PROBLEM_ID = 1L;

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    private Long problemId;

    // No-arg constructor required for Hibernate and OptaPlanner
    public AbstractPersistable() {
    }

    public AbstractPersistable(Long problemId) {
        this.problemId = problemId;
    }

    public AbstractPersistable(Long id, Long problemId) {
        this.id = id;
        this.problemId = problemId;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }
}
