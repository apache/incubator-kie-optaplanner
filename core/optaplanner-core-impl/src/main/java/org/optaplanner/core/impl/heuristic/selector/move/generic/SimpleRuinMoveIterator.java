package org.optaplanner.core.impl.heuristic.selector.move.generic;

import static org.optaplanner.core.impl.heuristic.move.CompositeMove.buildMove;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.InverseRelationShadowVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.chained.SubChainRuinMove;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ListUnassignMove;
import org.optaplanner.core.impl.heuristic.selector.value.chained.SubChain;

class SimpleRuinMoveIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    protected final EntitySelector<Solution_> originEntitySelector;
    protected final List<GenuineVariableDescriptor<Solution_>> variableDescriptorList;
    protected final Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors;
    protected final long numberOfRuinableEntities;

    private final HashMap<GenuineVariableDescriptor<Solution_>, SingletonInverseVariableSupply> variableDescriptorToInverseVariableSupply;

    protected final long numberOfEntitiesToBeRuined;
    protected Iterator<Object> originIterator;

    public SimpleRuinMoveIterator(EntitySelector<Solution_> originEntitySelector,
            List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            Collection<ShadowVariableDescriptor<Solution_>> shadowVariableDescriptors,
            HashMap<GenuineVariableDescriptor<Solution_>, SingletonInverseVariableSupply> variableDescriptorToInverseVariableSupply,
            long numberOfRuinableEntities, long numberOfEntitiesToBeRuined) {
        this.originEntitySelector = originEntitySelector;
        this.originIterator = originEntitySelector.iterator();
        this.variableDescriptorList = variableDescriptorList;
        this.shadowVariableDescriptors = shadowVariableDescriptors;
        this.variableDescriptorToInverseVariableSupply = variableDescriptorToInverseVariableSupply;
        this.numberOfRuinableEntities = numberOfRuinableEntities;
        this.numberOfEntitiesToBeRuined = numberOfEntitiesToBeRuined;
    }

    protected boolean validateSelectionNotNull(Object selectedEntity) {
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            if (variableDescriptor.isListVariable()
                    && ((ListVariableDescriptor<Solution_>) variableDescriptor).getListSize(selectedEntity) > 0) {
                return true;
            } else if (variableDescriptor.getValue(selectedEntity) != null) {
                return true;
            }
        }

        for (ShadowVariableDescriptor<Solution_> shadowVariableDescriptor : shadowVariableDescriptors) {
            if (shadowVariableDescriptor.getValue(selectedEntity) != null) {
                return true;
            }
        }
        return false;
    }

    protected Set<Object> chooseEntitiesToBeRuined() {
        Set<Object> entities = new HashSet<>();
        long checkedEntitiesCounter = 0;
        while (entities.size() < numberOfEntitiesToBeRuined && originIterator.hasNext()
                && checkedEntitiesCounter < numberOfRuinableEntities) {
            Object originSelection = originIterator.next();
            if (validateSelectionNotNull(originSelection)) {
                entities.add(originSelection);
            }

            ++checkedEntitiesCounter;
        }

        return entities;
    }

    private List<Move<Solution_>> buildChangeAndListUnassignRuinMoves(Set<Object> entities) {
        List<Move<Solution_>> changeAndListRuinMoves = new ArrayList<>();
        List<ListUnassignMove<Solution_>> listUnassignMoves = new ArrayList<>();
        for (Object entity : entities) {
            for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
                if (variableDescriptor.isGenuineListVariable()) {
                    ListVariableDescriptor<Solution_> listVariableDescriptor =
                            (ListVariableDescriptor<Solution_>) variableDescriptor;
                    List<Object> listVariable = listVariableDescriptor.getListVariable(entity);
                    // all list unassign moves depends on each other and actual order of the unassignments,
                    // but each of the composite moves in the moveList must not rely on the effect of a previous move in the moveList
                    // to create its undoMove correctly. Therefore, list unassigning backwards, so that indexes of the entities
                    // to be unassigned are not affected by one of the previous unassignment (they are not moved in the list).
                    for (int sourceIndex = listVariable.size() - 1; sourceIndex >= 0; --sourceIndex) {
                        changeAndListRuinMoves.add(new ListUnassignMove<>(listVariableDescriptor, entity, sourceIndex));
                    }
                } else if (!variableDescriptor.isChained()) {
                    changeAndListRuinMoves.add(new ChangeMove<>(variableDescriptor, entity, null));
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
                                changeAndListRuinMoves
                                        .add(new ChangeMove<>((GenuineVariableDescriptor<Solution_>) sourceVariableDescriptor,
                                                sourceEntity, null));
                            }
                        }
                    }
                }
            }
        }
        Comparator<ListUnassignMove<Solution_>> listUnassignMoveComparator =
                Comparator.comparing(ListUnassignMove::getSourceIndex);
        // all list unassign moves depends on each other and actual order of the unassignments,
        // but each of the composite moves in the moveList must not rely on the effect of a previous move in the moveList
        // to create its undoMove correctly. Therefore, list unassigning backwards, so that indexes of the entities
        // to be unassigned are not affected by one of the previous unassignment (they are not moved in the list).
        listUnassignMoves.sort(listUnassignMoveComparator.reversed());
        changeAndListRuinMoves.addAll(listUnassignMoves);
        return changeAndListRuinMoves;
    }

    private List<Move<Solution_>> buildSubChainRuinMoves(Set<Object> entities) {
        List<Move<Solution_>> subChainRuinMoves = new ArrayList<>();
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
                    subChainRuinMoves.add(new SubChainRuinMove<>(subChain, variableDescriptor,
                            variableDescriptorToInverseVariableSupply.get(variableDescriptor)));
                }
            }
        }
        return subChainRuinMoves;
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        // Ideally, this code should have read:
        //     SubS leftSubSelection = leftSubSelectionIterator.next();
        //     SubS rightSubSelection = rightSubSelectionIterator.next();
        // But empty selectors and ending selectors (such as non-random or shuffled) make it more complex
        if (!originIterator.hasNext()) {
            originIterator = originEntitySelector.iterator();
            if (!originIterator.hasNext()) {
                return noUpcomingSelection();
            }
        }

        Set<Object> entities = chooseEntitiesToBeRuined();
        if (entities.isEmpty()) {
            return noUpcomingSelection();
        }

        List<Move<Solution_>> combinedRuinMoves = new ArrayList<>(buildSubChainRuinMoves(entities));
        combinedRuinMoves.addAll(buildChangeAndListUnassignRuinMoves(entities));

        return buildMove(combinedRuinMoves);
    }
}
