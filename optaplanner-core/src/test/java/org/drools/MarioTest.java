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

import org.drools.core.base.accumulators.CountAccumulateFunction;
import org.drools.model.DSL;
import org.drools.model.Model;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.impl.ModelImpl;
import org.drools.model.view.ViewItem;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.drools.modelcompiler.dsl.pattern.D;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.assertj.core.api.Assertions.assertThat;

public class MarioTest {

    @Test
    void npeWithGroupByAfterExists() {
        Variable<List> groupResultVar = PatternDSL.globalOf(List.class, "defaultPkg", "glob");

        Variable<Integer> patternVar = PatternDSL.declarationOf(Integer.class);
        Variable<String> existsVar = PatternDSL.declarationOf(String.class);
        Variable<Integer> keyVar = PatternDSL.declarationOf(Integer.class);
        Variable<Integer> resultVar = PatternDSL.declarationOf(Integer.class);

        PatternDSL.PatternDef<Integer> pattern = PatternDSL.pattern(patternVar);
        PatternDSL.PatternDef<String> exist = PatternDSL.pattern(existsVar);
        ViewItem patternAndExists = PatternDSL.and(
                pattern,
                PatternDSL.exists(exist));

        ViewItem groupBy = PatternDSL.groupBy(patternAndExists, patternVar, keyVar, Math::abs,
                DSL.accFunction(CountAccumulateFunction::new).as(resultVar));
        ConsequenceBuilder._3 consequence = PatternDSL.on(keyVar, resultVar, groupResultVar)
                .execute((key, count, result) -> {
                    result.add(new Object[] { key, count });
                });

        Rule rule = D.rule("R").build(groupBy, consequence);
        Model model = new ModelImpl().addRule(rule);
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);
        KieSession session = kieBase.newKieSession();
        List<Object[]> global = new ArrayList<>();
        session.setGlobal("glob", global);

        session.insert("Something");
        session.insert(-1);
        session.insert(1);
        session.insert(2);
        session.fireAllRules();

        assertThat(global)
                .hasSize(2)
                .contains(new Object[] { 1, 2 }) // -1 and 1 will map to the same key, and count twice.
                .contains(new Object[] { 2, 1 }); // 2 maps to a key, and counts once.
    }

}
