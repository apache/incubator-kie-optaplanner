package org.optaplanner.core.impl.exhaustivesearch.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableSolution;

public class ExhaustiveSearchListener extends PhaseLifecycleListenerAdapter<TestdataComparableSolution> {

    private final List<String> dataConfigurations = new ArrayList<>();

    @Override
    public void stepEnded(AbstractStepScope<TestdataComparableSolution> abstractStepScope) {
        addConfiguration(abstractStepScope.getWorkingSolution());
    }

    @Override
    public void solvingEnded(DefaultSolverScope<TestdataComparableSolution> solverScope) {
        addConfiguration(solverScope.getBestSolution());
    }

    private void addConfiguration(TestdataComparableSolution solution) {
        dataConfigurations.add(solution.getEntityList().stream()
                                       .map(TestdataComparableEntity::getValue)
                                       .map(value -> value == null ? "-" : value.getCode())
                                       .collect(Collectors.joining()));
    }

    public List<String> getTestdataConfigurations() {
        return dataConfigurations;
    }
}
