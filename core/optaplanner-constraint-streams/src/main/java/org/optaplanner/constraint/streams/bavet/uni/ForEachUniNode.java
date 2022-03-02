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

package org.optaplanner.constraint.streams.bavet.uni;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;

public final class ForEachUniNode<A> extends AbstractNode {

    private final Class<A> forEachClass;
    private final UniOut<A>[] outs;

    private final Map<A, UniTuple<A>> tupleMap = new IdentityHashMap<>(1000);
    private final Queue<UniTuple<A>> dirtyTupleQueue;

    public ForEachUniNode(int nodeIndex, Class<A> forEachClass) {
        super(nodeIndex);
        this.forEachClass = forEachClass;
        outs = (UniOut<A>[]) Array.newInstance(UniOut.class, 5);
        dirtyTupleQueue = new ArrayDeque<>(1000);

        // TODO fill in outs
    }

    public void insert(A a) {
        UniTuple<A> tuple = new UniTuple<>(a);
        tuple.state = BavetTupleState.CREATING;
        UniTuple<A> old = tupleMap.put(a, tuple);
        if (old != null) {
            throw new IllegalStateException("The fact (" + a + ") was already inserted, so it cannot insert again.");
        }
        dirtyTupleQueue.add(tuple);
    }

    public void retract(A a) {
        UniTuple<A> tuple = tupleMap.remove(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot retract.");
        }
        tuple.state = BavetTupleState.DYING;
        dirtyTupleQueue.add(tuple);
    }

    public void calculateScore() {
        dirtyTupleQueue.forEach(tuple -> {
            // Retract
            if (tuple.state == BavetTupleState.UPDATING || tuple.state == BavetTupleState.DYING) {
                for (int outIndex = 0; outIndex < outs.length; outIndex++) {
                    UniOut<A> out = outs[outIndex];
                    out.nextNodeRetract.accept(tuple);
                }
            }
            // Insert
            if (tuple.state == BavetTupleState.CREATING || tuple.state == BavetTupleState.UPDATING) {
                for (int outIndex = 0; outIndex < outs.length; outIndex++) {
                    UniOut<A> out = outs[outIndex];
                    if (out.predicate.test(tuple.factA)) {
                        out.nextNodeInsert.accept(tuple);
                    }
                }
            }
            switch (tuple.state) {
                case CREATING:
                case UPDATING:
                    tuple.state = BavetTupleState.OK;
                    return;
                case DYING:
                case ABORTING:
                    tuple.state = BavetTupleState.DEAD;
                    return;
                case DEAD:
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                            this + ") is already in the dead state (" + tuple.state + ").");
                default:
                    throw new IllegalStateException("Impossible state: Tuple (" + tuple + ") in node (" +
                            this + ") is in an unexpected state (" + tuple.state + ").");
            }
        });
        dirtyTupleQueue.clear();
    }

    @Override
    public String toString() {
        return "ForEachUniNode(" + forEachClass.getSimpleName() + ")";
    }

}
