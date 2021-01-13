/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.uni;

import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.common.UniLeftHandSide;

public final class DroolsFromUniConstraintStream<Solution_, A> extends DroolsAbstractUniConstraintStream<Solution_, A> {

    private final Class<A> fromClass;
    private final UniLeftHandSide<A> leftHandSide;

    public DroolsFromUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory, Class<A> fromClass) {
        super(constraintFactory);
        this.fromClass = fromClass;
        this.leftHandSide = new UniLeftHandSide<>(fromClass, constraintFactory.getVariableFactory());
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public UniLeftHandSide<A> getLeftHandSide() {
        return leftHandSide;
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public String toString() {
        return "From(" + fromClass.getSimpleName() + ") with " + getChildStreams().size() + " children";
    }

}
