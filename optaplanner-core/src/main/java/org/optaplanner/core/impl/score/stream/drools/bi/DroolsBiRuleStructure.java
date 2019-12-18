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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsPatternBuilder;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;

public class DroolsBiRuleStructure<A, B> extends DroolsRuleStructure {

    private final Variable<A> a;
    private final Variable<B> b;
    private final DroolsPatternBuilder<?> targetPattern;
    private final List<RuleItemBuilder<?>> openRuleItems;
    private final List<RuleItemBuilder<?>> closedRuleItems;

    /**
     * Builds a final version of the A pattern as it will no longer be mutated, and turns the B pattern into the new
     * primary pattern.
     * @param aRuleStructure
     * @param bRuleStructure
     * @param variableIdSupplier
     */
    public DroolsBiRuleStructure(DroolsUniRuleStructure<A> aRuleStructure, DroolsUniRuleStructure<B> bRuleStructure,
            LongSupplier variableIdSupplier) {
        super(variableIdSupplier);
        this.a = aRuleStructure.getA();
        this.b = bRuleStructure.getA();
        this.targetPattern = bRuleStructure.getPrimaryPattern();
        List<RuleItemBuilder<?>> newOpenItems = new ArrayList<>();
        newOpenItems.addAll(aRuleStructure.getOpenRuleItems());
        newOpenItems.add(aRuleStructure.getPrimaryPattern().build());
        newOpenItems.addAll(bRuleStructure.getOpenRuleItems());
        this.openRuleItems = Collections.unmodifiableList(newOpenItems);
        List<RuleItemBuilder<?>> newClosedItems = new ArrayList<>();
        newClosedItems.addAll(aRuleStructure.getClosedRuleItems());
        newClosedItems.addAll(bRuleStructure.getClosedRuleItems());
        this.closedRuleItems = Collections.unmodifiableList(newClosedItems);
    }

    public DroolsBiRuleStructure(Variable<A> aVariable, Variable<B> bVariable, DroolsPatternBuilder<?> targetPattern,
            List<RuleItemBuilder<?>> openRuleItems, List<RuleItemBuilder<?>> closedRuleItems,
            LongSupplier variableIdSupplier) {
        super(variableIdSupplier);
        this.a = aVariable;
        this.b = bVariable;
        this.targetPattern = targetPattern;
        this.openRuleItems = Collections.unmodifiableList(openRuleItems);
        this.closedRuleItems = Collections.unmodifiableList(closedRuleItems);
    }

    public Variable<A> getA() {
        return a;
    }

    public Variable<B> getB() {
        return b;
    }

    @Override
    public DroolsPatternBuilder<Object> getPrimaryPattern() {
        return (DroolsPatternBuilder<Object>) targetPattern;
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
