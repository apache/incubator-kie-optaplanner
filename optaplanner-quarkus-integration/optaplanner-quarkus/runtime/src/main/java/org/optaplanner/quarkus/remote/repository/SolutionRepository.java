package org.optaplanner.quarkus.remote.repository;

public interface SolutionRepository<Solution, ProblemId> {
    void save(ProblemId problemId, Solution solution);

    Solution load(ProblemId problemId);
}
