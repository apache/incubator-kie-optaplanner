package org.optaplanner.core.impl.testdata.phase.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableSolution;

public class TestdataSolutionSateRecorder extends PhaseLifecycleListenerAdapter<TestdataComparableSolution> {

    private final List<String> solutionStates = new ArrayList<>();

    @Override
    public void stepEnded(AbstractStepScope<TestdataComparableSolution> abstractStepScope) {
        addSolutionState(abstractStepScope.getWorkingSolution());
    }

    @Override
    public void solvingEnded(DefaultSolverScope<TestdataComparableSolution> solverScope) {
        addSolutionState(solverScope.getBestSolution());
    }

    private void addSolutionState(TestdataComparableSolution solution) {
        solutionStates.add(solution.getEntityList().stream()
                                       .map(TestdataComparableEntity::getValue)
                                       .map(value -> value == null ? "-" : value.getCode())
                                       .collect(Collectors.joining()));
    }

    public List<String> getSolutionStates() {
        return solutionStates;
    }
}
