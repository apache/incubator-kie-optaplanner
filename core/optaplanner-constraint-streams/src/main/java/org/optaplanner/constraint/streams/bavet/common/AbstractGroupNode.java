
/*
 *
 *  * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.optaplanner.constraint.streams.bavet.common;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.bi.JoinBiNode;
import org.optaplanner.constraint.streams.bavet.uni.UniScorer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

public abstract class AbstractGroupNode<InTuple_ extends Tuple, OutTuple_ extends Tuple, GroupKey_, ResultContainer_, Result_>
        extends AbstractNode {

    private final int groupStoreIndex;
    private final Supplier<ResultContainer_> supplier;
    protected final Function<ResultContainer_, Result_> finisher;
    /**
     * Some code paths may decide to not supply a collector.
     * In that case, we skip the code path that would attempt to use it.
     */
    private final boolean runAccumulate;
    /**
     * Calls for example {@link UniScorer#insert(UniTuple)}, {@link JoinBiNode#insertA(UniTuple)} and/or ...
     */
    private final Consumer<OutTuple_> nextNodesInsert;
    /**
     * Calls for example {@link UniScorer#retract(UniTuple)}, {@link JoinBiNode#retractA(UniTuple)} and/or ...
     */
    private final Consumer<OutTuple_> nextNodesRetract;
    private final Map<GroupKey_, Group<OutTuple_, GroupKey_, ResultContainer_>> groupMap;
    private final Queue<Group<OutTuple_, GroupKey_, ResultContainer_>> dirtyGroupQueue;

    protected AbstractGroupNode(int groupStoreIndex, Supplier<ResultContainer_> supplier,
            Function<ResultContainer_, Result_> finisher, Consumer<OutTuple_> nextNodesInsert,
            Consumer<OutTuple_> nextNodesRetract) {
        this.groupStoreIndex = groupStoreIndex;
        this.supplier = supplier;
        this.finisher = finisher;
        this.runAccumulate = supplier != null && finisher != null;
        this.nextNodesInsert = nextNodesInsert;
        this.nextNodesRetract = nextNodesRetract;
        groupMap = new HashMap<>(1000);
        dirtyGroupQueue = new ArrayDeque<>(1000);
    }

    public void insert(InTuple_ tupleOldA) {
        if (tupleOldA.getStore()[groupStoreIndex] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tupleOldA
                    + ") was already added in the tupleStore.");
        }
        GroupKey_ groupKey = createGroupKey(tupleOldA);
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupMap.computeIfAbsent(groupKey,
                k -> new Group<>(groupKey, runAccumulate ? supplier.get() : null));
        group.parentCount++;

        Runnable undoAccumulator = runAccumulate ? accumulate(group.resultContainer, tupleOldA) : null;
        GroupPart<OutTuple_, GroupKey_, ResultContainer_> groupPart = new GroupPart<>(group, undoAccumulator);
        tupleOldA.getStore()[groupStoreIndex] = groupPart;
        if (!group.dirty) {
            group.dirty = true;
            dirtyGroupQueue.add(group);
        }
    }

    protected abstract GroupKey_ createGroupKey(InTuple_ tuple);

    protected abstract Runnable accumulate(ResultContainer_ resultContainer, InTuple_ tuple);

    protected abstract OutTuple_ createDownstreamTuple(Group<OutTuple_, GroupKey_, ResultContainer_> group);

    public void retract(InTuple_ tupleOldA) {
        GroupPart<OutTuple_, GroupKey_, ResultContainer_> groupPart =
                (GroupPart<OutTuple_, GroupKey_, ResultContainer_>) tupleOldA.getStore()[groupStoreIndex];
        if (groupPart == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        tupleOldA.getStore()[groupStoreIndex] = null;
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupPart.group;
        group.parentCount--;
        if (runAccumulate) {
            groupPart.undoAccumulator.run();
        }
        if (group.parentCount == 0) {
            Group<OutTuple_, GroupKey_, ResultContainer_> old = groupMap.remove(group.groupKey);
            if (old == null) {
                throw new IllegalStateException("Impossible state: the group for the groupKey ("
                        + group.groupKey + ") doesn't exist in the groupMap.");
            }
            group.dying = true;
        }
        if (!group.dirty) {
            group.dirty = true;
            dirtyGroupQueue.add(group);
        }
    }

    @Override
    public void calculateScore() {
        dirtyGroupQueue.forEach(group -> {
            group.dirty = false;
            if (group.tuple != null) {
                BavetTupleState tupleState = group.tuple.getState();
                if (tupleState != BavetTupleState.OK) {
                    throw new IllegalStateException("Impossible state: The tuple (" + group.tuple + ") in node (" +
                            this + ") is in the state (" + tupleState + ").");
                }
                group.tuple.setState(BavetTupleState.DYING);
                nextNodesRetract.accept(group.tuple);
                group.tuple.setState(BavetTupleState.DEAD);
            }
            if (!group.dying) {
                // Delay calculating B until it propagates
                group.tuple = createDownstreamTuple(group);
                nextNodesInsert.accept(group.tuple);
                group.tuple.setState(BavetTupleState.OK);
            }
        });
        dirtyGroupQueue.clear();
    }

}
