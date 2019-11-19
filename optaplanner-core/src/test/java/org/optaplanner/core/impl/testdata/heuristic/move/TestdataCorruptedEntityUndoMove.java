package org.optaplanner.core.impl.testdata.heuristic.move;

import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

public class TestdataCorruptedEntityUndoMove extends AbstractTestdataMove {

    public TestdataCorruptedEntityUndoMove(TestdataEntity entity, TestdataValue toValue) {
        super(entity, toValue);
    }

    @Override
    protected AbstractMove<TestdataSolution> createUndoMove(ScoreDirector<TestdataSolution> scoreDirector) {
        return new TestdataCorruptedEntityUndoMove(new TestdataEntity("corrupted"), toValue);
    }
}
