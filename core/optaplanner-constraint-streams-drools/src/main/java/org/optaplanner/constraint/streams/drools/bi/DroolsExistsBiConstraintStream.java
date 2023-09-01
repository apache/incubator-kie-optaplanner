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

package org.optaplanner.constraint.streams.drools.bi;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.drools.DroolsConstraintFactory;
import org.optaplanner.constraint.streams.drools.common.BiLeftHandSide;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;

public final class DroolsExistsBiConstraintStream<Solution_, A, B>
        extends DroolsAbstractBiConstraintStream<Solution_, A, B> {

    private final DroolsAbstractBiConstraintStream<Solution_, A, B> parent;
    private final Supplier<BiLeftHandSide<A, B>> leftHandSide;
    private final String streamName;

    public <C> DroolsExistsBiConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, boolean shouldExist,
            boolean shouldIncludeNullVars, Class<C> otherClass, TriJoiner<A, B, C>... joiners) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        Predicate<C> nullityFilter = shouldIncludeNullVars ? null : constraintFactory.getNullityFilter(otherClass);
        this.leftHandSide = () -> shouldExist
                ? parent.createLeftHandSide().andExists(otherClass, joiners, nullityFilter)
                : parent.createLeftHandSide().andNotExists(otherClass, joiners, nullityFilter);
        this.streamName = shouldExist ? "BiIfExists()" : "BiIfNotExists()";
    }

    @Override
    public boolean guaranteesDistinct() {
        return parent.guaranteesDistinct();
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public BiLeftHandSide<A, B> createLeftHandSide() {
        return leftHandSide.get();
    }

    @Override
    public String toString() {
        return streamName + " with " + getChildStreams().size() + " children";
    }

}
