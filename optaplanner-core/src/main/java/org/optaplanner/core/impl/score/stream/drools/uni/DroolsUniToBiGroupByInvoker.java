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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.util.function.Function;

import org.drools.model.Variable;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupBy;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupByInvoker;

public class DroolsUniToBiGroupByInvoker<A, ResultContainer, NewA, NewB>
    extends DroolsAbstractGroupByInvoker<ResultContainer, A> {

    private final UniConstraintCollector<A, ResultContainer, NewB> collector;
    private final Function<A, NewA> groupKeyMapping;
    private final Variable<A> aVariable;

    public DroolsUniToBiGroupByInvoker(Function<A, NewA> groupKeyMapping,
            UniConstraintCollector<A, ResultContainer, NewB> collector, Variable<A> aVariable) {
        this.collector = collector;
        this.groupKeyMapping = groupKeyMapping;
        this.aVariable = aVariable;
    }

    @Override
    protected DroolsAbstractGroupBy<ResultContainer, A, ?> newContext() {
        return new DroolsUniToBiGroupBy<>(groupKeyMapping, collector);
    }

    @Override
    protected <X> A createInput(Function<Variable<X>, X> valueFinder) {
        return materialize(aVariable, valueFinder);
    }

}
