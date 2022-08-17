package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SubListSwapMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final SubList leftSubList;
    private final SubList rightSubList;
    private final boolean reversing;
    private final int rightFromIndex;
    private final int leftToIndex;

    public SubListSwapMove(ListVariableDescriptor<Solution_> variableDescriptor,
            SubList leftSubList,
            SubList rightSubList,
            boolean reversing) {
        this.variableDescriptor = variableDescriptor;
        if (leftSubList.getEntity() == rightSubList.getEntity() && leftSubList.getFromIndex() > rightSubList.getFromIndex()) {
            this.leftSubList = rightSubList;
            this.rightSubList = leftSubList;
        } else {
            this.leftSubList = leftSubList;
            this.rightSubList = rightSubList;
        }
        this.reversing = reversing;
        rightFromIndex = this.rightSubList.getFromIndex();
        leftToIndex = this.leftSubList.getToIndex();
    }

    public SubList getLeftSubList() {
        return leftSubList;
    }

    public SubList getRightSubList() {
        return rightSubList;
    }

    public boolean isReversing() {
        return reversing;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        // If both subLists are on the same entity, then they must not overlap.
        return leftSubList.getEntity() != rightSubList.getEntity() || rightFromIndex >= leftToIndex;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        if (leftSubList.getEntity() == rightSubList.getEntity()) {
            return new SubListSwapMove<>(variableDescriptor,
                    new SubList(leftSubList.getEntity(), leftSubList.getFromIndex(), rightSubList.getLength()),
                    new SubList(rightSubList.getEntity(),
                            rightSubList.getFromIndex() - leftSubList.getLength() + rightSubList.getLength(),
                            leftSubList.getLength()),
                    reversing);
        }
        return new SubListSwapMove<>(variableDescriptor,
                new SubList(rightSubList.getEntity(), rightSubList.getFromIndex(), leftSubList.getLength()),
                new SubList(leftSubList.getEntity(), leftSubList.getFromIndex(), rightSubList.getLength()),
                reversing);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;

        Object leftEntity = leftSubList.getEntity();
        Object rightEntity = rightSubList.getEntity();
        int leftFromIndex = leftSubList.getFromIndex();
        List<Object> leftList = variableDescriptor.getListVariable(leftEntity);
        List<Object> rightList = variableDescriptor.getListVariable(rightEntity);
        List<Object> leftSubListView = leftList.subList(leftFromIndex, leftSubList.getToIndex());
        List<Object> rightSubListView = rightList.subList(rightFromIndex, rightSubList.getToIndex());
        List<Object> leftSubListCopy = new ArrayList<>(leftSubListView);
        List<Object> rightSubListCopy = new ArrayList<>(rightSubListView);
        if (reversing) {
            Collections.reverse(leftSubListCopy);
            Collections.reverse(rightSubListCopy);
        }

        int rightSubListDestinationIndex = leftFromIndex;
        int leftSubListDestinationIndex = leftEntity != rightEntity ? rightFromIndex
                : rightFromIndex + rightSubList.getLength() - leftSubList.getLength();

        innerScoreDirector.beforeSubListChanged(variableDescriptor, leftEntity, rightSubListDestinationIndex,
                rightSubListDestinationIndex + rightSubList.getLength());
        innerScoreDirector.beforeSubListChanged(variableDescriptor, rightEntity, leftSubListDestinationIndex,
                leftSubListDestinationIndex + leftSubList.getLength());

        rightSubListView.clear();
        if (leftEntity != rightEntity) {
            leftSubListView.clear();
        } else {
            leftList.subList(leftFromIndex, leftToIndex).clear();
        }
        leftList.addAll(rightSubListDestinationIndex, rightSubListCopy);
        rightList.addAll(leftSubListDestinationIndex, leftSubListCopy);

        innerScoreDirector.afterSubListChanged(variableDescriptor, leftEntity, rightSubListDestinationIndex,
                rightSubListDestinationIndex + rightSubList.getLength());
        innerScoreDirector.afterSubListChanged(variableDescriptor, rightEntity, leftSubListDestinationIndex,
                leftSubListDestinationIndex + leftSubList.getLength());
    }

    @Override
    public String toString() {
        return "{" + leftSubList + "} <-" + (reversing ? "reversing-" : "") + "> {" + rightSubList + "}";
    }
}
