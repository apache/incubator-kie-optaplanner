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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.drools.model.Drools;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsVariableFactory;

import static org.drools.model.DSL.and;

abstract class AbstractLeftHandSide implements Supplier<List<ViewItem<?>>> {

    public static <A> UniLeftHandSide<A> forVariable(Variable<A> variable, DroolsVariableFactory variableFactory) {
        return new UniLeftHandSide<>(variable, variableFactory);
    }

    protected final DroolsVariableFactory variableFactory;

    protected AbstractLeftHandSide(DroolsVariableFactory variableFactory) {
        this.variableFactory = Objects.requireNonNull(variableFactory);
    }

    protected static <A> ViewItem<?> joinViewItemsWithLogicalAnd(PatternVariable<?>... patternVariables) {
        List<ViewItem<?>> viewItemList = new ArrayList<>();
        for (PatternVariable<?> patternVariable : patternVariables) {
            viewItemList.addAll(patternVariable.build());
        }
        int viewItemListSize = viewItemList.size();
        ViewItem<?> firstPattern = viewItemList.get(0);
        if (viewItemListSize == 1) {
            return firstPattern;
        }
        ViewItem<?>[] remainingPatternArray = viewItemList.subList(1, viewItemListSize)
                .toArray(new ViewItem[0]);
        return and(firstPattern, remainingPatternArray);
    }

    abstract public Variable[] getVariables();

}
