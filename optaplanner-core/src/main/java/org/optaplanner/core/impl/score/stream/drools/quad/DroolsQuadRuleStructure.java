/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.quad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsPatternBuilder;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;

public class DroolsQuadRuleStructure<A, B, C, D> extends DroolsRuleStructure {

    private final Variable<A> a;
    private final Variable<B> b;
    private final Variable<C> c;
    private final Variable<D> d;
    private final DroolsPatternBuilder<?> primaryPattern;
    private final List<RuleItemBuilder<?>> openRuleItems;
    private final List<RuleItemBuilder<?>> closedRuleItems;

    /**
     * Builds a final version of the ABC pattern as it will no longer be mutated, and turns the D pattern into the new
     * primary pattern.
     * @param abcRuleStructure
     * @param dRuleStructure
     * @param variableIdSupplier
     */
    public DroolsQuadRuleStructure(DroolsTriRuleStructure<A, B, C> abcRuleStructure,
            DroolsUniRuleStructure<D> dRuleStructure, LongSupplier variableIdSupplier) {
        super(variableIdSupplier);
        this.a = abcRuleStructure.getA();
        this.b = abcRuleStructure.getB();
        this.c = abcRuleStructure.getC();
        this.d = dRuleStructure.getA();
        this.primaryPattern = dRuleStructure.getPrimaryPattern();
        List<RuleItemBuilder<?>> newOpenItems = new ArrayList<>();
        newOpenItems.addAll(abcRuleStructure.getOpenRuleItems());
        newOpenItems.add(abcRuleStructure.getPrimaryPattern().build());
        newOpenItems.addAll(dRuleStructure.getOpenRuleItems());
        this.openRuleItems = Collections.unmodifiableList(newOpenItems);
        List<RuleItemBuilder<?>> newClosedItems = new ArrayList<>();
        newClosedItems.addAll(abcRuleStructure.getClosedRuleItems());
        newClosedItems.addAll(dRuleStructure.getClosedRuleItems());
        this.closedRuleItems = Collections.unmodifiableList(newClosedItems);
    }

    public DroolsQuadRuleStructure(Variable<A> aVariable, Variable<B> bVariable, Variable<C> cVariable,
            Variable<D> dVariable, DroolsPatternBuilder<?> primaryPattern,
            List<RuleItemBuilder<?>> openRuleItems, LongSupplier variableIdSupplier) {
        super(variableIdSupplier);
        this.a = aVariable;
        this.b = bVariable;
        this.c = cVariable;
        this.d = dVariable;
        this.primaryPattern = primaryPattern;
        this.openRuleItems = Collections.unmodifiableList(openRuleItems);
        this.closedRuleItems = Collections.emptyList();
    }

    public Variable<A> getA() {
        return a;
    }

    public Variable<B> getB() {
        return b;
    }

    public Variable<C> getC() {
        return c;
    }

    public Variable<D> getD() {
        return d;
    }

    @Override
    public DroolsPatternBuilder<Object> getPrimaryPattern() {
        return (DroolsPatternBuilder<Object>) primaryPattern;
    }

    @Override
    public List<RuleItemBuilder<?>> getOpenRuleItems() {
        return openRuleItems;
    }

    @Override
    public List<RuleItemBuilder<?>> getClosedRuleItems() {
        return closedRuleItems;
    }
}
