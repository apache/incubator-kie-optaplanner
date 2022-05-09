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

package org.optaplanner.constraint.streams.bavet.bi;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.bi.BiScorer;
import org.optaplanner.constraint.streams.bavet.bi.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.bavet.tri.JoinTriNode;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

abstract class AbstractGroupBiNode<OldA, OldB, OutTuple_ extends Tuple, GroupKey_, ResultContainer_, Result_>
        extends AbstractNode {

    private final int groupStoreIndex;
    private final Supplier<ResultContainer_> supplier;
    private final TriFunction<ResultContainer_, OldA, OldB, Runnable> accumulator;
    protected final Function<ResultContainer_, Result_> finisher;
    /**
     * Calls for example {@link BiScorer#insert(BiTuple)}, {@link JoinTriNode#insertAB(BiTuple)} and/or ...
     */
    private final Consumer<OutTuple_> nextNodesInsert;
    /**
     * Calls for example {@link BiScorer#retract(BiTuple)}, {@link JoinTriNode#retractAB(BiTuple)} and/or ...
     */
    private final Consumer<OutTuple_> nextNodesRetract;
    private final Map<GroupKey_, Group<OutTuple_, GroupKey_, ResultContainer_>> groupMap;
    private final Queue<Group<OutTuple_, GroupKey_, ResultContainer_>> dirtyGroupQueue;

    public AbstractGroupBiNode(int groupStoreIndex, BiConstraintCollector<OldA, OldB, ResultContainer_, Result_> collector,
            Consumer<OutTuple_> nextNodesInsert, Consumer<OutTuple_> nextNodesRetract) {
        this.groupStoreIndex = groupStoreIndex;
        supplier = collector.supplier();
        accumulator = collector.accumulator();
        finisher = collector.finisher();
        this.nextNodesInsert = nextNodesInsert;
        this.nextNodesRetract = nextNodesRetract;
        groupMap = new HashMap<>(1000);
        dirtyGroupQueue = new ArrayDeque<>(1000);
    }

    public static final class Group<OutTuple_ extends Tuple, GroupKey_, ResultContainer_> {
        public GroupKey_ groupKey;
        public ResultContainer_ resultContainer;
        int parentCount = 0;
        boolean dirty = false;
        boolean dying = false;
        OutTuple_ tuple = null;

        public Group(GroupKey_ groupKey, ResultContainer_ resultContainer) {
            this.groupKey = groupKey;
            this.resultContainer = resultContainer;
        }
    }

    private static final class GroupPart<OutTuple_ extends Tuple, GroupKey_, ResultContainer_> {
        Group<OutTuple_, GroupKey_, ResultContainer_> group;
        Runnable undoAccumulator;

        public GroupPart(Group<OutTuple_, GroupKey_, ResultContainer_> group, Runnable undoAccumulator) {
            this.group = group;
            this.undoAccumulator = undoAccumulator;
        }
    }

    public void insertAB(BiTuple<OldA, OldB> tupleOldAB) {
        if (tupleOldAB.store[groupStoreIndex] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tupleOldAB
                    + ") was already added in the tupleStore.");
        }
        GroupKey_ groupKey = getGroupKey(tupleOldAB.factA, tupleOldAB.factB);
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupMap.computeIfAbsent(groupKey,
                k -> new Group<>(groupKey, supplier.get()));
        group.parentCount++;

        Runnable undoAccumulator = accumulator.apply(group.resultContainer, tupleOldAB.factA, tupleOldAB.factB);
        GroupPart<OutTuple_, GroupKey_, ResultContainer_> groupPart = new GroupPart<>(group, undoAccumulator);
        tupleOldAB.store[groupStoreIndex] = groupPart;
        if (!group.dirty) {
            group.dirty = true;
            dirtyGroupQueue.add(group);
        }
    }

    protected abstract GroupKey_ getGroupKey(OldA a, OldB b);

    protected abstract OutTuple_ createTuple(Group<OutTuple_, GroupKey_, ResultContainer_> group);

    public void retractAB(BiTuple<OldA, OldB> tupleOldAB) {
        GroupPart<OutTuple_, GroupKey_, ResultContainer_> groupPart =
                (GroupPart<OutTuple_, GroupKey_, ResultContainer_>) tupleOldAB.store[groupStoreIndex];
        if (groupPart == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        tupleOldAB.store[groupStoreIndex] = null;
        Group<OutTuple_, GroupKey_, ResultContainer_> group = groupPart.group;
        group.parentCount--;
        groupPart.undoAccumulator.run();
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
                group.tuple = createTuple(group);
                nextNodesInsert.accept(group.tuple);
                group.tuple.setState(BavetTupleState.OK);
            }
        });
        dirtyGroupQueue.clear();
    }

}
