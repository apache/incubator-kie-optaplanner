package org.acme.common.message;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SolverRequest {
    private Long problemId;

    SolverRequest() {
        // Required for JSON deserialization.
    }

    public SolverRequest(Long problemId) {
        this.problemId = problemId;
    }

    public Long getProblemId() {
        return problemId;
    }
}
