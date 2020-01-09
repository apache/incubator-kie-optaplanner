package org.optaplanner.core.impl.exhaustivesearch.event;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableEntity;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableSolution;
import org.optaplanner.core.impl.testdata.domain.comparable.TestdataComparableValue;

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
        StringBuilder s = new StringBuilder();

        for(TestdataComparableEntity entity : solution.getEntityList()) {
            TestdataComparableValue value = entity.getValue();
            if(value == null) {
                s.append("-");
            }else {
                s.append(value.getCode());
            }
        }
        dataConfigurations.add(s.toString());
    }

    public List<String> getTestdataConfigurations() {
        return dataConfigurations;
    }
}
