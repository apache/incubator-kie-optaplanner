package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Collections;

import org.openjdk.jmh.annotations.Benchmark;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractPlannerMoveMicroBenchmark;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplanner.core.impl.score.director.easy.EasyScoreDirector;
import org.optaplanner.core.impl.score.director.easy.EasyScoreDirectorFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

public class ChangeMoveBenchmark extends AbstractPlannerMoveMicroBenchmark<TestdataSolution> {

    private TestdataValue testdataValue = new TestdataValue("v1");
    private TestdataEntity testdataEntity;
    private SolutionDescriptor<TestdataSolution> solutionDescriptor;

    @Override
    public void initMove() {
        EntityDescriptor<TestdataSolution> entityDescriptor = solutionDescriptor.
                findEntityDescriptorOrFail(TestdataEntity.class);
        GenuineVariableDescriptor<TestdataSolution> genuineVariableDescriptor = entityDescriptor.
                getGenuineVariableDescriptor("value");
        ChangeMove<TestdataSolution> testdataSolutionChangeMove =
                new ChangeMove<>(testdataEntity, genuineVariableDescriptor, testdataValue);
        super.setMove(testdataSolutionChangeMove);
    }

    @Override
    protected void initEntities() {
        testdataEntity = new TestdataEntity("e1");
        solutionDescriptor = SolutionDescriptor.buildSolutionDescriptor(TestdataSolution.class, TestdataEntity.class);
    }

    @Override
    public void initScoreDirector() {
        EasyScoreCalculator<TestdataSolution> easyScoreCalculator = testdataSolution -> SimpleScore.of(0);

        EasyScoreDirector<TestdataSolution> testdataSolutionEasyScoreDirector = new EasyScoreDirector<>
                (new EasyScoreDirectorFactory<>(solutionDescriptor,
                                                easyScoreCalculator),
                 true,
                 true,
                 easyScoreCalculator);
        TestdataSolution workingSolution = new TestdataSolution();
        workingSolution.setValueList(Collections.singletonList(testdataValue));
        workingSolution.setEntityList(Collections.singletonList(testdataEntity));
        testdataSolutionEasyScoreDirector.setWorkingSolution(workingSolution);
        super.setScoreDirector(testdataSolutionEasyScoreDirector);
    }

    @Benchmark
    public Move<TestdataSolution> benchmarkDoMove() {
        return super.benchmarkDoMove();
    }

    @Benchmark
    public Move<TestdataSolution> benchmarkRebase() {
        return super.benchmarkRebase();
    }
}
