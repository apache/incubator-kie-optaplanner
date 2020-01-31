package org.optaplanner.core.impl.testdata.phase.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataDifficultyComparingEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataDifficultyComparingSolution;

public class TestdataSolutionSateRecorder extends PhaseLifecycleListenerAdapter<TestdataDifficultyComparingSolution> {

    private final List<String> workingSolutions = new ArrayList<>();

    @Override
    public void stepEnded(AbstractStepScope<TestdataDifficultyComparingSolution> abstractStepScope) {
        addWorkingSolution(abstractStepScope.getWorkingSolution());
    }

    @Override
    public void solvingEnded(DefaultSolverScope<TestdataDifficultyComparingSolution> solverScope) {
        addWorkingSolution(solverScope.getBestSolution());
    }

    private void addWorkingSolution(TestdataDifficultyComparingSolution solution) {
        workingSolutions.add(solution.getEntityList().stream()
                                       .map(TestdataDifficultyComparingEntity::getValue)
                                       .map(value -> value == null ? "-" : value.getCode())
                                       .collect(Collectors.joining()));
    }

    public List<String> getWorkingSolutions() {
        return workingSolutions;
    }
}
