package org.optaplanner.core.impl.testdata.domain.shadow;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class TestdataShadowVariableListener implements VariableListener<TestdataShadowEntity> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, TestdataShadowEntity entity) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, TestdataShadowEntity entity) {
        updateStartingPoint(scoreDirector, entity);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, TestdataShadowEntity entity) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, TestdataShadowEntity entity) {
        updateStartingPoint(scoreDirector, entity);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, TestdataShadowEntity entity) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, TestdataShadowEntity entity) {

    }

    protected void updateStartingPoint(ScoreDirector scoreDirector, TestdataShadowEntity source) {
        TestdataShadowIface previousIface = source.getPreviousEntity();
        if(previousIface == null) {
            scoreDirector.beforeVariableChanged(source, "forwardSum");
            source.setForwardSum(0);
            scoreDirector.afterVariableChanged(source, "forwardSum");
            return;
        }
        Integer summary = (previousIface instanceof TestdataShadowEntity) ? ((TestdataShadowEntity) previousIface).getForwardSum() : 0;
        TestdataShadowEntity currentEntity = source;
        Integer sumWithCurrentEntity = summary + Math.abs(currentEntity.getValue() - previousIface.getValue());
        while (true) {
            scoreDirector.beforeVariableChanged(currentEntity, "forwardSum");
            currentEntity.setForwardSum(sumWithCurrentEntity);
            scoreDirector.afterVariableChanged(currentEntity, "forwardSum");
            currentEntity = currentEntity.getNextEntity();
            if(currentEntity == null) {
                return;
            }
            sumWithCurrentEntity += Math.abs(currentEntity.getValue() - currentEntity.getPreviousEntity().getValue());
        }
    }
}