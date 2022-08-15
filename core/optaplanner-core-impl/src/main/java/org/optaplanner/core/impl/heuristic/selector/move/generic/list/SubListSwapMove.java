package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SubListSwapMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final SubList leftSubList;
    private final SubList rightSubList;

    public SubListSwapMove(ListVariableDescriptor<Solution_> variableDescriptor, SubList leftSubList, SubList rightSubList) {
        this.variableDescriptor = variableDescriptor;
        this.leftSubList = leftSubList;
        this.rightSubList = rightSubList;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // If both subLists are on the same entity, then they must not overlap.
        return leftSubList.getEntity() != rightSubList.getEntity()
                || leftSubList.getFromIndex() >= rightSubList.getFromIndex() + rightSubList.getLength()
                || rightSubList.getFromIndex() >= leftSubList.getFromIndex() + leftSubList.getLength();
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        return new SubListSwapMove<>(variableDescriptor,
                new SubList(rightSubList.getEntity(), rightSubList.getFromIndex(), leftSubList.getLength()),
                new SubList(leftSubList.getEntity(), leftSubList.getFromIndex(), rightSubList.getLength()));
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        List<Object> leftList = variableDescriptor.getListVariable(leftSubList.getEntity());
        List<Object> rightList = variableDescriptor.getListVariable(rightSubList.getEntity());
        List<Object> leftSubListView = leftList.subList(leftSubList.getFromIndex(), leftSubList.getToIndex());
        List<Object> rightSubListView = rightList.subList(rightSubList.getFromIndex(), rightSubList.getToIndex());
        List<Object> leftSubListCopy = new ArrayList<>(leftSubListView);
        List<Object> rightSubListCopy = new ArrayList<>(rightSubListView);
        leftSubListView.clear();
        rightSubListView.clear();
        leftList.addAll(leftSubList.getFromIndex(), rightSubListCopy);
        rightList.addAll(rightSubList.getFromIndex(), leftSubListCopy);
    }

    @Override
    public String toString() {
        return "{" + leftSubList + "} <-> {" + rightSubList + "}";
    }
}
