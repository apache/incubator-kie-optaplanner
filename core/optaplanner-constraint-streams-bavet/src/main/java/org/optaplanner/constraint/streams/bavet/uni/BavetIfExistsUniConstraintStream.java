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

package org.optaplanner.constraint.streams.bavet.uni;

import java.util.Set;
import java.util.function.BiPredicate;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.common.AbstractIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexerFactory;
import org.optaplanner.constraint.streams.bavet.common.index.JoinerUtils;
import org.optaplanner.constraint.streams.common.bi.DefaultBiJoiner;
import org.optaplanner.core.api.score.Score;

public final class BavetIfExistsUniConstraintStream<Solution_, A, B> extends BavetAbstractUniConstraintStream<Solution_, A> {

    private final BavetAbstractUniConstraintStream<Solution_, A> parentA;
    private final BavetIfExistsBridgeUniConstraintStream<Solution_, B> parentBridgeB;

    private final boolean shouldExist;
    private final DefaultBiJoiner<A, B> joiner;
    private final BiPredicate<A, B> filtering;

    public BavetIfExistsUniConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractUniConstraintStream<Solution_, A> parentA,
            BavetIfExistsBridgeUniConstraintStream<Solution_, B> parentBridgeB,
            boolean shouldExist,
            DefaultBiJoiner<A, B> joiner, BiPredicate<A, B> filtering) {
        super(constraintFactory, parentA.getRetrievalSemantics());
        this.parentA = parentA;
        this.parentBridgeB = parentBridgeB;
        this.shouldExist = shouldExist;
        this.joiner = joiner;
        this.filtering = filtering;
    }

    @Override
    public boolean guaranteesDistinct() {
        return parentA.guaranteesDistinct();
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        parentA.collectActiveConstraintStreams(constraintStreamSet);
        parentBridgeB.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public BavetAbstractConstraintStream<Solution_> getTupleSource() {
        return parentA.getTupleSource();
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        TupleLifecycle<UniTuple<A>> downstream = buildHelper.getAggregatedTupleLifecycle(childStreamList);
        IndexerFactory indexerFactory = new IndexerFactory(joiner);
        AbstractIfExistsNode<UniTuple<A>, B> node = indexerFactory.hasJoiners()
                ? (filtering == null ? new IndexedIfExistsUniNode<>(shouldExist,
                        JoinerUtils.combineLeftMappings(joiner), JoinerUtils.combineRightMappings(joiner),
                        buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                        buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                        buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                        buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                        downstream, indexerFactory.buildIndexer(true), indexerFactory.buildIndexer(false))
                        : new IndexedIfExistsUniNode<>(shouldExist,
                                JoinerUtils.combineLeftMappings(joiner), JoinerUtils.combineRightMappings(joiner),
                                buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                                downstream, indexerFactory.buildIndexer(true), indexerFactory.buildIndexer(false),
                                filtering))
                : (filtering == null ? new UnindexedIfExistsUniNode<>(shouldExist,
                        buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                        buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()), downstream)
                        : new UnindexedIfExistsUniNode<>(shouldExist,
                                buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentA.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                                buildHelper.reserveTupleStoreIndex(parentBridgeB.getTupleSource()),
                                downstream, filtering));
        buildHelper.addNode(node, this, parentBridgeB);
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    // TODO

    @Override
    public String toString() {
        return "IfExists() with " + childStreamList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
