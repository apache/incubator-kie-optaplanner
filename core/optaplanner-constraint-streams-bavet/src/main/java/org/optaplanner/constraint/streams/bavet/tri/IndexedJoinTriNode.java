/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.bavet.tri;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.bi.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.function.TriPredicate;

final class IndexedJoinTriNode<A, B, C>
        extends AbstractIndexedJoinNode<BiTuple<A, B>, C, TriTuple<A, B, C>, TriTupleImpl<A, B, C>> {

    private final BiFunction<A, B, IndexProperties> mappingAB;
    private final TriPredicate<A, B, C> filtering;
    private final int outputStoreSize;

    public IndexedJoinTriNode(BiFunction<A, B, IndexProperties> mappingAB, Function<C, IndexProperties> mappingC,
            int inputStoreIndexAB, int inputStoreIndexEntryAB, int inputStoreIndexOutTupleListAB,
            int inputStoreIndexC, int inputStoreIndexEntryC, int inputStoreIndexOutTupleListC,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, TriPredicate<A, B, C> filtering,
            int outputStoreSize,
            int outputStoreIndexOutEntryAB, int outputStoreIndexOutEntryC,
            Indexer<BiTuple<A, B>> indexerAB,
            Indexer<UniTuple<C>> indexerC) {
        super(mappingC,
                inputStoreIndexAB, inputStoreIndexEntryAB, inputStoreIndexOutTupleListAB,
                inputStoreIndexC, inputStoreIndexEntryC, inputStoreIndexOutTupleListC,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexOutEntryAB, outputStoreIndexOutEntryC,
                indexerAB, indexerC);
        this.mappingAB = mappingAB;
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(BiTuple<A, B> leftTuple) {
        return mappingAB.apply(leftTuple.getFactA(), leftTuple.getFactB());
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return new TriTupleImpl<>(leftTuple.getFactA(), leftTuple.getFactB(), rightTuple.getFactA(), outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(TriTupleImpl<A, B, C> outTuple, BiTuple<A, B> leftTuple) {
        outTuple.factA = leftTuple.getFactA();
        outTuple.factB = leftTuple.getFactB();
    }

    @Override
    protected void setOutTupleRightFact(TriTupleImpl<A, B, C> outTuple, UniTuple<C> rightTuple) {
        outTuple.factC = rightTuple.getFactA();
    }

    @Override
    protected boolean testFiltering(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return filtering.test(leftTuple.getFactA(), leftTuple.getFactB(), rightTuple.getFactA());
    }

}
