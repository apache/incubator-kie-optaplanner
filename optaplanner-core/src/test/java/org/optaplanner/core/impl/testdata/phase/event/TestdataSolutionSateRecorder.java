package org.optaplanner.core.impl.testdata.phase.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataEntityWithDifficultyComparator;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataSolutionWithDifficultyComparatorEntity;

public class TestdataSolutionSateRecorder extends PhaseLifecycleListenerAdapter<TestdataSolutionWithDifficultyComparatorEntity> {

    private final List<String> workingSolutions = new ArrayList<>();

    @Override
    public void stepEnded(AbstractStepScope<TestdataSolutionWithDifficultyComparatorEntity> abstractStepScope) {
        addWorkingSolution(abstractStepScope.getWorkingSolution());
    }

    @Override
    public void solvingEnded(DefaultSolverScope<TestdataSolutionWithDifficultyComparatorEntity> solverScope) {
        addWorkingSolution(solverScope.getBestSolution());
    }

    private void addWorkingSolution(TestdataSolutionWithDifficultyComparatorEntity solution) {
        workingSolutions.add(solution.getEntityList().stream()
                                       .map(TestdataEntityWithDifficultyComparator::getValue)
                                       .map(value -> value == null ? "-" : value.getCode())
                                       .collect(Collectors.joining()));
    }

    public List<String> getWorkingSolutions() {
        return workingSolutions;
    }
}
