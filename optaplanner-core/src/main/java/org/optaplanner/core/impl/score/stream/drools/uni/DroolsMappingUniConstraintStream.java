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

import java.util.function.Function;

import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.common.UniLeftHandSide;

public final class DroolsMappingUniConstraintStream<Solution_, NewA>
        extends DroolsAbstractUniConstraintStream<Solution_, NewA> {

    private final UniLeftHandSide<NewA> leftHandSide;

    public <A> DroolsMappingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> mappingFunction) {
        super(constraintFactory, parent);
        this.leftHandSide = parent.getLeftHandSide().andMap(mappingFunction);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public UniLeftHandSide<NewA> getLeftHandSide() {
        return leftHandSide;
    }

    @Override
    public String toString() {
        return "Map() with " + getChildStreams().size() + " children";
    }

}
