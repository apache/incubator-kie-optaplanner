package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class ListKOptMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final int k;
    private final List<List2OptMove<Solution_>> equivalent2Opts;

    public ListKOptMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
                        int k, List<List2OptMove<Solution_>> equivalent2Opts) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.k = k;
        this.equivalent2Opts = equivalent2Opts;
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        if (equivalent2Opts.isEmpty()) {
            return this;
        } else {
            Object entity = equivalent2Opts.get(0).getEntity();
            Object[] oldValues = listVariableDescriptor.getListVariable(entity).toArray();
            return new UndoListKOptMove(entity, oldValues);
        }
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        equivalent2Opts.forEach(move -> move.doMoveOnGenuineVariables(scoreDirector));
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return !equivalent2Opts.isEmpty();
    }

    @Override
    public Move<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        List<List2OptMove<Solution_>> rebasedEquivalent2Opts = new ArrayList<>(equivalent2Opts.size());
        for (List2OptMove<Solution_> twoOpt : equivalent2Opts) {
            rebasedEquivalent2Opts.add(twoOpt.rebase(destinationScoreDirector));
        }
        return new ListKOptMove<>(listVariableDescriptor, k, rebasedEquivalent2Opts);
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return k + "-opt(" + listVariableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(equivalent2Opts.get(0).getEntity());
    }

    public String toString() {
        return k + "-opt(equivalent2Opt="
                + equivalent2Opts.stream().map(List2OptMove::toString).collect(Collectors.joining(", ", "[", "]"))
                + ")";
    }

    private final class UndoListKOptMove extends AbstractMove<Solution_> {
        final Object entity;
        final Object[] oldValues;

        public UndoListKOptMove(Object entity, Object[] oldValues) {
            this.entity = entity;
            this.oldValues = oldValues;
        }

        @Override
        public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
            return true;
        }

        @Override
        protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
            InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;

            List<Object> listVariable = listVariableDescriptor.getListVariable(entity);

            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, 0, oldValues.length);

            for (int i = 0; i < oldValues.length; i++) {
                listVariable.set(i, oldValues[i]);
            }

            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, 0, oldValues.length);
        }
    }

}
