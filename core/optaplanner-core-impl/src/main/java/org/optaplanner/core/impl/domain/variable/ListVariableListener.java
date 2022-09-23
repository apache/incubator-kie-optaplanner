package org.optaplanner.core.impl.domain.variable;

import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public interface ListVariableListener<Solution_, Entity_> extends AbstractVariableListener<Solution_, Entity_> {

    void afterListVariableElementRemoved(ScoreDirector<Solution_> scoreDirector, Object element);

    void beforeListVariableChanged(ScoreDirector<Solution_> scoreDirector, Entity_ entity, int fromIndex, int toIndex);

    void afterListVariableChanged(ScoreDirector<Solution_> scoreDirector, Entity_ entity, int fromIndex, int toIndex);
}
