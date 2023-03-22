package org.optaplanner.core.impl.heuristic.selector.move.generic;

import static org.optaplanner.core.impl.heuristic.move.CompositeMove.buildMove;

import java.util.*;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.InverseRelationShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableDemand;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainRuinMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ListUnassignMove;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChain;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public class CompositeRuinMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final EntitySelector<Solution_> leftEntitySelector;
    private final EntitySelector<Solution_> rightEntitySelector;
    private final List<GenuineVariableDescriptor<Solution_>> variableDescriptorList;
    private final Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors;
    private final Integer percentageToBeRuined;

    private final HashMap<GenuineVariableDescriptor<Solution_>, SingletonInverseVariableSupply> variableDescriptorToInverseVariableSupply =
            new HashMap<>();
    private final boolean chained;

    public CompositeRuinMoveSelector(EntitySelector<Solution_> leftEntitySelector,
            EntitySelector<Solution_> rightEntitySelector,
            List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors, Integer percentageToBeRuined) {
        assert 0 < percentageToBeRuined && percentageToBeRuined <= 100
                : "CompositeRuinMoveSelector: percentage to ruin the solution must be greater than 0 and smaller or equal to 100%";
        this.leftEntitySelector = leftEntitySelector;
        this.rightEntitySelector = rightEntitySelector;
        this.variableDescriptorList = variableDescriptorList;
        this.shadowVariableDescriptors = shadowVariableDescriptors;
        this.percentageToBeRuined = percentageToBeRuined;
        this.chained = this.variableDescriptorList.stream().anyMatch(GenuineVariableDescriptor::isChained);

        phaseLifecycleSupport.addEventListener(leftEntitySelector);
        if (leftEntitySelector != rightEntitySelector) {
            phaseLifecycleSupport.addEventListener(rightEntitySelector);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return leftEntitySelector.isCountable() && rightEntitySelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return true;
    }

    @Override
    public long getSize() {
        return percentageToBeRuined * leftEntitySelector.getSize() / 100;
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        if (chained) {
            SupplyManager supplyManager = solverScope.getScoreDirector().getSupplyManager();
            for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                if (variableDescriptor.isChained()) {
                    variableDescriptorToInverseVariableSupply.put(variableDescriptor,
                            supplyManager.demand(new SingletonInverseVariableDemand<>(variableDescriptor)));
                }
            }
        }
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        if (chained) {
            variableDescriptorToInverseVariableSupply.clear();
        }
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        return new RuinMoveIterator();
    }

    private class RuinMoveIterator extends UpcomingSelectionIterator<Move<Solution_>> {

        private Iterator<Object> leftIterator;

        private RuinMoveIterator() {
            leftIterator = leftEntitySelector.iterator();
        }

        @Override
        protected Move<Solution_> createUpcomingSelection() {
            // Ideally, this code should have read:
            //     SubS leftSubSelection = leftSubSelectionIterator.next();
            //     SubS rightSubSelection = rightSubSelectionIterator.next();
            // But empty selectors and ending selectors (such as non-random or shuffled) make it more complex
            if (!leftIterator.hasNext()) {
                leftIterator = leftEntitySelector.iterator();
                if (!leftIterator.hasNext()) {
                    return noUpcomingSelection();
                }
            }

            Object leftSubSelection;
            Set<Object> entities = new HashSet<>();
            int counter = 0;
            long leftSize = leftEntitySelector.getSize();
            while (entities.isEmpty() && leftIterator.hasNext()) {
                leftSubSelection = leftIterator.next();
                for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                    if (variableDescriptor.isListVariable()
                            && ((ListVariableDescriptor<Solution_>) variableDescriptor).getListSize(leftSubSelection) > 0) {
                        entities.add(leftSubSelection);
                        break;
                    } else if (variableDescriptor.getValue(leftSubSelection) != null) {
                        entities.add(leftSubSelection);
                        break;
                    }
                }

                for (ShadowVariableDescriptor<Solution_> shadowVariableDescriptor : shadowVariableDescriptors) {
                    if (shadowVariableDescriptor.getValue(leftSubSelection) != null) {
                        entities.add(leftSubSelection);
                        break;
                    }
                }
                counter++;

                if (counter >= leftSize) {
                    return noUpcomingSelection();
                }
            }

            long rightSize = rightEntitySelector.getSize();
            Iterator<Object> rightIterator = rightEntitySelector.iterator();

            counter = 0;
            while (entities.size() < getSize() && rightIterator.hasNext()) {
                Object rightSubSelection = rightIterator.next();
                for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                    if (variableDescriptor.isListVariable()
                            && ((ListVariableDescriptor<Solution_>) variableDescriptor).getListSize(rightSubSelection) > 0) {
                        entities.add(rightSubSelection);
                        break;
                    } else if (variableDescriptor.getValue(rightSubSelection) != null) {
                        entities.add(rightSubSelection);
                        break;
                    }
                }

                for (ShadowVariableDescriptor<Solution_> shadowVariableDescriptor : shadowVariableDescriptors) {
                    if (shadowVariableDescriptor.getValue(rightSubSelection) != null) {
                        entities.add(rightSubSelection);
                        break;
                    }
                }
                counter++;

                if (counter >= rightSize) {
                    return noUpcomingSelection();
                }
            }

            List<Move<Solution_>> moves = new ArrayList<>();
            for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                if (variableDescriptor.isChained()) {
                    HashMap<Object, Object> prevToNext = new HashMap<>();
                    Set<Object> subAnchors = new HashSet<>();
                    for (Object entity : entities) {
                        Object previousEntity =
                                variableDescriptorToInverseVariableSupply.get(variableDescriptor).getInverseSingleton(entity);
                        if (entities.contains(previousEntity)) {
                            prevToNext.put(previousEntity, entity);
                        } else {
                            subAnchors.add(entity);
                        }
                    }

                    for (Object subAnchor : subAnchors) {
                        List<Object> chainedEntities = new ArrayList<>();

                        Object previous = subAnchor;
                        chainedEntities.add(previous);
                        while (prevToNext.containsKey(previous)) {
                            Object next = prevToNext.get(previous);
                            chainedEntities.add(0, next);
                            previous = next;
                        }
                        SubChain subChain = new SubChain(chainedEntities);
                        moves.add(new SubChainRuinMove<>(subChain, variableDescriptor,
                                variableDescriptorToInverseVariableSupply.get(variableDescriptor)));
                    }
                }
            }

            List<ListUnassignMove<Solution_>> listUnassignMoves = new ArrayList<>();
            for (Object entity : entities) {
                for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                    if (variableDescriptor.isGenuineListVariable()) {
                        ListVariableDescriptor<Solution_> listVariableDescriptor =
                                (ListVariableDescriptor<Solution_>) variableDescriptor;
                        List<Object> listVariable = listVariableDescriptor.getListVariable(entity);
                        for (int sourceIndex = listVariable.size() - 1; sourceIndex >= 0; --sourceIndex) {
                            moves.add(new ListUnassignMove<>(listVariableDescriptor, entity, sourceIndex));
                        }
                    } else if (!variableDescriptor.isChained()) {
                        moves.add(new ChangeMove<>(variableDescriptor, entity, null));
                    }
                }

                for (ShadowVariableDescriptor<Solution_> shadowVariableDescriptor : shadowVariableDescriptors) {
                    if (shadowVariableDescriptor instanceof InverseRelationShadowVariableDescriptor) {
                        Object sourceEntity = shadowVariableDescriptor.getValue(entity);
                        if (sourceEntity != null) {
                            List<VariableDescriptor<Solution_>> sourceVariableDescriptors =
                                    shadowVariableDescriptor.getSourceVariableDescriptorList();
                            for (VariableDescriptor<Solution_> sourceVariableDescriptor : sourceVariableDescriptors) {
                                if (sourceVariableDescriptor.isGenuineListVariable()) {
                                    ListVariableDescriptor<Solution_> listVariableDescriptor =
                                            (ListVariableDescriptor<Solution_>) sourceVariableDescriptor;
                                    List<Object> listVariable = listVariableDescriptor.getListVariable(sourceEntity);
                                    int sourceIndex = listVariable.indexOf(entity);
                                    listUnassignMoves
                                            .add(new ListUnassignMove<>(listVariableDescriptor, sourceEntity, sourceIndex));
                                } else {
                                    moves.add(new ChangeMove<>((GenuineVariableDescriptor<Solution_>) sourceVariableDescriptor,
                                            sourceEntity, null));
                                }
                            }
                        }
                    }
                }
            }
            Comparator<ListUnassignMove<Solution_>> listUnassignMoveComparator =
                    Comparator.comparing(ListUnassignMove::getSourceIndex);
            listUnassignMoves.sort(listUnassignMoveComparator.reversed());
            moves.addAll(listUnassignMoves);

            return buildMove(moves);
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + leftEntitySelector + ", " + rightEntitySelector + ")";
    }

}
