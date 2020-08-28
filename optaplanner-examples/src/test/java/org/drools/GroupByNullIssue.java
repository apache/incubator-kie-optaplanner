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

package org.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.drools.core.base.accumulators.CountAccumulateFunction;
import org.drools.model.DSL;
import org.drools.model.Model;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.impl.ModelImpl;
import org.drools.model.view.ExprViewItem;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.rule;

public class GroupByNullIssue {

    private static final class MyType {

        private final MyType nested;

        public MyType(MyType nested) {
            this.nested = nested;
        }

        public MyType getNested() {
            return nested;
        }


    }

    @Test
    public void nullIssue() {

        Variable<MyType> var = PatternDSL.declarationOf(MyType.class);
        Variable<MyType> groupKey = PatternDSL.declarationOf(MyType.class);
        Variable<Long> count = PatternDSL.declarationOf(Long.class);

        AtomicInteger mappingFunctionCallCounter = new AtomicInteger(0);
        Function1<MyType, MyType> mappingFunction = (a) -> {
            mappingFunctionCallCounter.incrementAndGet();
            return a.getNested();
        };
        PatternDSL.PatternDef<MyType> onlyOnesWithNested = PatternDSL.pattern(var)
                .expr(myType -> myType.getNested() != null);
        ExprViewItem groupBy = PatternDSL.groupBy(onlyOnesWithNested, var, groupKey, mappingFunction,
                DSL.accFunction(CountAccumulateFunction::new).as(count));

        List<MyType> result = new ArrayList<>();

        Rule rule = rule("R")
                .build(groupBy,
                        on(groupKey, count)
                                .execute((drools, key, acc) -> result.add(key)));

        Model model = new ModelImpl().addRule( rule );
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel( model );

        MyType objectWithoutNestedObject = new MyType(null);
        MyType objectWithNestedObject = new MyType(objectWithoutNestedObject);
        KieSession ksession = kieBase.newKieSession();
        ksession.insert(objectWithNestedObject);
        ksession.insert(objectWithoutNestedObject);
        ksession.fireAllRules();

        // Side issue: this number is unusually high. Perhaps we should try to implement some cache for this?
        System.out.println("GroupKey mapping function was called " + mappingFunctionCallCounter.get() + " times.");

        // Bug? Even though line 71 prevents null, they are still here.
        Assertions.assertThat(result)
                .containsOnly(objectWithoutNestedObject);

    }

}
