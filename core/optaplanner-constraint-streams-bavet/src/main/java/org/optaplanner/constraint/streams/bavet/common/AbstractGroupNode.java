
/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.constraint.streams.bavet.common;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractGroupNode<InTuple_ extends Tuple, OutTuple_ extends Tuple, GroupKey_, ResultContainer_, Result_>
        extends AbstractNode {

    private final int groupStoreIndex;
    private final Supplier<ResultContainer_> supplier;
    private final Function<ResultContainer_, Result_> finisher;
    /**
     * Some code paths may decide to not supply a collector.
     * In that case, we skip the code path that would attempt to use it.
     */
    private final boolean hasCollector;
    /**
     * Calls for example {@link AbstractScorer#insert(Tuple)}, and/or ...
     */
    private final Consumer<OutTuple_> nextNodesInsert;
    /**
     * Calls for example {@link AbstractScorer#update(Tuple)}, and/or ...
     */
    private final Consumer<OutTuple_> nextNodesUpdate;
    /**
     * Calls for example {@link AbstractScorer#retract(Tuple)}, and/or ...
     */
    private final Consumer<OutTuple_> nextNodesRetract;
    private final Map<GroupKey_, Group<OutTuple_, GroupKey_, ResultContainer_>> groupMap;
    private final Queue<Group<OutTuple_, GroupKey_, ResultContainer_>> dirtyGroupQueue;

    protected AbstractGroupNode(int groupStoreIndex,
            Supplier<ResultContainer_> supplier,
            Function<ResultContainer_, Result_> finisher,
            Consumer<OutTuple_> nextNodesInsert,
            Consumer<OutTuple_> nextNodesUpdate,
            Consumer<OutTuple_> nextNodesRetract) {
        this.groupStoreIndex = groupStoreIndex;
        this.supplier = supplier;
        this.finisher = finisher;
        this.hasCollector = supplier != null;
        this.nextNodesInsert = nextNodesInsert;
        this.nextNodesUpdate = nextNodesUpdate;
        this.nextNodesRetract = nextNodesRetract;
        this.groupMap = new HashMap<>(1000);
        this.dirtyGroupQueue = new ArrayDeque<>(1000);
    }

    public void insert(InTuple_ tuple) {
        Object[] tupleStore = tuple.getStore();
        if (tupleStore[groupStoreIndex] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tuple
                    + ") was already added in the tupleStore.");
        }
        GroupKey_ groupKey = createGroupKey(tuple);
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupMap.computeIfAbsent(groupKey,
                k -> new Group<>(k, hasCollector ? supplier.get() : null));
        group.parentCount++;

        Runnable undoAccumulator = hasCollector ? accumulate(group.resultContainer, tuple) : null;
        GroupPart<Group<OutTuple_, GroupKey_, ResultContainer_>> groupPart = new GroupPart<>(group, undoAccumulator);
        tupleStore[groupStoreIndex] = groupPart;

        if (group.state == null) {
            group.state = BavetTupleState.CREATING;
            dirtyGroupQueue.add(group);
        } else {
            switch (group.state) {
                case CREATING:
                    break;
                case UPDATING:
                    break;
                case OK:
                    group.state = BavetTupleState.UPDATING;
                    dirtyGroupQueue.add(group);
                    break;
                case DYING:
                    group.state = BavetTupleState.UPDATING;
                    break;
                case ABORTING:
                    group.state = BavetTupleState.CREATING;
                    break;
                case DEAD:
                default:
                    throw new IllegalStateException("Impossible state: The group (" + group + ") in node (" +
                            this + ") is in an unexpected state (" + group.state + ").");
            }
        }
    }

    public void update(InTuple_ tuple) {
        // TODO Implement actual update
        retract(tuple);
        insert(tuple);
    }

    public void retract(InTuple_ tuple) {
        Object[] tupleStore = tuple.getStore();
        GroupPart<Group<OutTuple_, GroupKey_, ResultContainer_>> groupPart =
                (GroupPart<Group<OutTuple_, GroupKey_, ResultContainer_>>) tupleStore[groupStoreIndex];
        if (groupPart == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        tupleStore[groupStoreIndex] = null;
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupPart.group;
        group.parentCount--;
        if (hasCollector) {
            groupPart.undoAccumulator.run();
        }
        boolean killGroup = (group.parentCount == 0);
        if (killGroup) {
            GroupKey_ groupKey = group.groupKey;
            Group<OutTuple_, GroupKey_, ResultContainer_> old = groupMap.remove(groupKey);
            if (old == null) {
                throw new IllegalStateException("Impossible state: the group for the groupKey ("
                        + groupKey + ") doesn't exist in the groupMap.");
            }
        }
        switch (group.state) {
            case CREATING:
                if (killGroup) {
                    group.state = BavetTupleState.ABORTING;
                }
                break;
            case UPDATING:
                if (killGroup) {
                    group.state = BavetTupleState.DYING;
                }
                break;
            case OK:
                group.state = killGroup ? BavetTupleState.DYING : BavetTupleState.UPDATING;
                dirtyGroupQueue.add(group);
                break;
            case DYING:
            case ABORTING:
            case DEAD:
            default:
                throw new IllegalStateException("Impossible state: The group (" + group + ") in node (" +
                        this + ") is in an unexpected state (" + group.state + ").");
        }
    }

    protected abstract GroupKey_ createGroupKey(InTuple_ tuple);

    protected abstract Runnable accumulate(ResultContainer_ resultContainer, InTuple_ tuple);

    @Override
    public void calculateScore() {
        for (Group<OutTuple_, GroupKey_, ResultContainer_> group : dirtyGroupQueue) {
            OutTuple_ outTuple = group.outTuple;
            if (outTuple != null && outTuple.getState() != BavetTupleState.OK) {
                throw new IllegalStateException("Impossible state: The outTuple (" + group.outTuple + ") in node (" +
                        this + ") is in the state (" + group.outTuple.getState() + ").");
            }
            // Delay calculating finisher right until the tuple propagates
            switch (group.state) {
                case CREATING:
                    outTuple = createOutTuple(group.groupKey);
                    updateOutTupleToFinisher(outTuple, group.resultContainer);
                    group.outTuple = outTuple;
                    outTuple.setState(BavetTupleState.CREATING);
                    nextNodesInsert.accept(outTuple);
                    outTuple.setState(BavetTupleState.OK);
                    group.state = BavetTupleState.OK;
                    break;
                case UPDATING:
                    updateOutTupleToFinisher(group.outTuple, group.resultContainer);
                    outTuple.setState(BavetTupleState.UPDATING);
                    nextNodesUpdate.accept(outTuple);
                    outTuple.setState(BavetTupleState.OK);
                    group.state = BavetTupleState.OK;
                    break;
                case DYING:
                    outTuple.setState(BavetTupleState.DYING);
                    nextNodesRetract.accept(outTuple);
                    outTuple.setState(BavetTupleState.DEAD);
                    group.state = BavetTupleState.DEAD;
                    break;
                case ABORTING:
                    group.state = BavetTupleState.DEAD;
                    break;
                case OK:
                case DEAD:
                default:
                    throw new IllegalStateException("Impossible state: The group (" + group + ") in node (" +
                            this + ") is in an unexpected state (" + group.state + ").");
            }
        }
        dirtyGroupQueue.clear();
    }

    protected abstract OutTuple_ createOutTuple(GroupKey_ groupKey);

    private void updateOutTupleToFinisher(OutTuple_ outTuple, ResultContainer_ resultContainer) {
        if (finisher == null) {
            return;
        }
        Result_ result = finisher.apply(resultContainer);
        updateOutTupleToResult(outTuple, result);
    }

    protected abstract void updateOutTupleToResult(OutTuple_ outTuple, Result_ result);

}
