/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.variable.inverserelation;

import java.util.Objects;

import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;
import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class SingletonInverseVariableDemand<Solution_> implements Demand<Solution_, SingletonInverseVariableSupply> {

    protected final VariableDescriptor<Solution_> sourceVariableDescriptor;

    public SingletonInverseVariableDemand(VariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    public VariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    // ************************************************************************
    // Creation method
    // ************************************************************************

    @Override
    public SingletonInverseVariableSupply createExternalizedSupply(InnerScoreDirector<Solution_, ?> scoreDirector) {
        return new ExternalizedSingletonInverseVariableSupply<>(sourceVariableDescriptor);
    }

    // ************************************************************************
    // Equals/hashCode method
    // ************************************************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SingletonInverseVariableDemand)) {
            return false;
        }
        SingletonInverseVariableDemand<Solution_> other = (SingletonInverseVariableDemand<Solution_>) o;
        if (!sourceVariableDescriptor.equals(other.sourceVariableDescriptor)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(SingletonInverseVariableDemand.class.getName(), sourceVariableDescriptor);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceVariableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

}
