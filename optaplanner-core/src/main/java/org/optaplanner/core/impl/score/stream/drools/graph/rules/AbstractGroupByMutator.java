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

package org.optaplanner.core.impl.score.stream.drools.graph.rules;

import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.drools.model.DSL.accFunction;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.drools.model.DSL;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractAccumulateFunction;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupByAccumulator;
import org.optaplanner.core.impl.score.stream.drools.common.FactTuple;
import org.optaplanner.core.impl.score.stream.drools.common.QuadTuple;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;

abstract class AbstractGroupByMutator implements Mutator {

    protected abstract <InTuple> PatternDef bindTupleVariableOnFirstGrouping(AbstractRuleBuilder ruleBuilder,
            PatternDef pattern, Variable<InTuple> tupleVariable);

    protected ViewItem<?> getInnerAccumulatePattern(AbstractRuleBuilder ruleBuilder) {
        List<ViewItem> allPatterns = new ArrayList<>();
        for (int i = 0; i < ruleBuilder.getPrimaryPatterns().size(); i++) {
            allPatterns.add(ruleBuilder.getPrimaryPatterns().get(i));
            allPatterns.addAll(ruleBuilder.getDependentExpressionMap().getOrDefault(i, Collections.emptyList()));
        }
        ViewItem[] items = allPatterns.toArray(new ViewItem[0]);
        return PatternDSL.and(items[0], copyOfRange(items, 1, items.length));
    }

    protected <NewA, InTuple, OutTuple> AbstractRuleBuilder collect(AbstractRuleBuilder ruleBuilder,
            DroolsAbstractAccumulateFunction<?, InTuple, OutTuple> accumulateFunctionBridge) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        PatternDef mainAccumulatePattern = ruleBuilder.getPrimaryPatterns().get(ruleBuilder.getPrimaryPatterns().size() - 1);
        boolean isRegrouping = FactTuple.class.isAssignableFrom(mainAccumulatePattern.getFirstVariable().getType());
        Variable<InTuple> tupleVariable = isRegrouping ? mainAccumulatePattern.getFirstVariable()
                : Util.createVariable(ruleBuilder.generateNextId("tuple"));
        if (!isRegrouping) {
            bindTupleVariableOnFirstGrouping(ruleBuilder, mainAccumulatePattern, tupleVariable);
        }
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(ruleBuilder);
        Variable<NewA> outputVariable = Util.createVariable(ruleBuilder.generateNextId("collected"));
        ViewItem<?> outerAccumulatePattern = DSL.accumulate(innerAccumulatePattern,
                accFunction(() -> accumulateFunctionBridge, tupleVariable).as(outputVariable));
        return recollect(ruleBuilder, outputVariable, outerAccumulatePattern);
    }

    protected <InTuple> AbstractRuleBuilder groupWithCollect(AbstractRuleBuilder ruleBuilder,
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier) {
        return universalGroupWithCollect(ruleBuilder, invokerSupplier,
                (var, pattern, accumulate) -> regroupBi(ruleBuilder, (Variable) var, pattern, accumulate));
    }

    private <InTuple> AbstractRuleBuilder universalGroupWithCollect(AbstractRuleBuilder ruleBuilder,
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier, Transformer<InTuple> mutator) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(ruleBuilder);
        Variable<Collection<InTuple>> tupleCollection =
                (Variable<Collection<InTuple>>) Util.createVariable(Collection.class,
                        ruleBuilder.generateNextId("tupleCollection"));
        PatternDef<Collection<InTuple>> pattern = pattern(tupleCollection)
                .expr("Non-empty", collection -> !collection.isEmpty(),
                        alphaIndexedBy(Integer.class, Index.ConstraintType.GREATER_THAN, -1, Collection::size, 0));
        ViewItem<?> accumulate = DSL.accumulate(innerAccumulatePattern, accFunction(invokerSupplier).as(tupleCollection));
        return mutator.apply(tupleCollection, pattern, accumulate);
    }

    protected <InTuple> AbstractRuleBuilder groupBiWithCollect(AbstractRuleBuilder ruleBuilder,
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier) {
        return universalGroupWithCollect(ruleBuilder, invokerSupplier,
                (var, pattern, accumulate) -> regroupBiToTri(ruleBuilder, (Variable) var, pattern, accumulate));
    }

    protected <InTuple> AbstractRuleBuilder groupBiWithCollectBi(AbstractRuleBuilder ruleBuilder,
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier) {
        return universalGroupWithCollect(ruleBuilder, invokerSupplier,
                (var, pattern, accumulate) -> regroupBiToQuad(ruleBuilder, (Variable) var, pattern, accumulate));
    }

    protected <NewA> AbstractRuleBuilder recollect(AbstractRuleBuilder ruleBuilder, Variable<NewA> newA,
            ViewItem accumulatePattern) {
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleBuilder.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern); // The last pattern is added here.
        PatternDef<NewA> newPrimaryPattern = PatternDSL.pattern(newA);
        return new UniRuleBuilder(ruleBuilder::generateNextId, ruleBuilder.getExpectedGroupByCount(),
                newFinishedExpressions, singletonList(newA), singletonList(newPrimaryPattern), emptyMap());
    }

    public <NewA> AbstractRuleBuilder regroup(AbstractRuleBuilder ruleBuilder, Variable<Collection<NewA>> newASource,
            ViewItem collectPattern, ViewItem accumulatePattern) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleBuilder.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern);
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA =
                (Variable<NewA>) Util.createVariable(ruleBuilder.generateNextId("uniGrouped"), DSL.from(newASource));
        PatternDef<NewA> newPrimaryPattern = PatternDSL.pattern(newA);
        return new UniRuleBuilder(ruleBuilder::generateNextId, ruleBuilder.getExpectedGroupByCount(),
                newFinishedExpressions, singletonList(newA), singletonList(newPrimaryPattern), emptyMap());
    }

    public <NewA, NewB> AbstractRuleBuilder regroupBi(AbstractRuleBuilder ruleBuilder,
            Variable<Collection<BiTuple<NewA, NewB>>> newSource, ViewItem collectPattern, ViewItem accumulatePattern) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        Variable<BiTuple<NewA, NewB>> newTuple =
                (Variable<BiTuple<NewA, NewB>>) Util.createVariable(BiTuple.class,
                        ruleBuilder.generateNextId("biGrouped"), PatternDSL.from(newSource));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleBuilder.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern);
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA = Util.createVariable(ruleBuilder.generateNextId("newA"));
        Variable<NewB> newB = Util.createVariable(ruleBuilder.generateNextId("newB"));
        List<Variable> newVariables = Arrays.asList(newA, newB);
        PatternDef<BiTuple<NewA, NewB>> newPrimaryPattern = PatternDSL.pattern(newTuple)
                .bind(newA, tuple -> tuple.a)
                .bind(newB, tuple -> tuple.b);
        return new BiRuleBuilder(ruleBuilder::generateNextId, ruleBuilder.getExpectedGroupByCount(),
                newFinishedExpressions, newVariables, singletonList(newPrimaryPattern), emptyMap());
    }

    public <NewA, NewB, NewC> AbstractRuleBuilder regroupBiToTri(AbstractRuleBuilder ruleBuilder,
            Variable<Set<TriTuple<NewA, NewB, NewC>>> newSource, ViewItem collectPattern,
            ViewItem accumulatePattern) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleBuilder.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern);
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA = Util.createVariable(ruleBuilder.generateNextId("newA"));
        Variable<NewB> newB = Util.createVariable(ruleBuilder.generateNextId("newB"));
        Variable<NewC> newC = Util.createVariable(ruleBuilder.generateNextId("newC"));
        List<Variable> newVariables = Arrays.asList(newA, newB, newC);
        Variable<TriTuple<NewA, NewB, NewC>> newTuple =
                (Variable<TriTuple<NewA, NewB, NewC>>) Util.createVariable(TriTuple.class,
                        ruleBuilder.generateNextId("triGrouped"), PatternDSL.from(newSource));
        PatternDef<TriTuple<NewA, NewB, NewC>> newPrimaryPattern = PatternDSL.pattern(newTuple)
                .bind(newA, tuple -> tuple.a)
                .bind(newB, tuple -> tuple.b)
                .bind(newC, tuple -> tuple.c);
        return new TriRuleBuilder(ruleBuilder::generateNextId, ruleBuilder.getExpectedGroupByCount(),
                newFinishedExpressions, newVariables, singletonList(newPrimaryPattern), emptyMap());
    }

    public <NewA, NewB, NewC, NewD> AbstractRuleBuilder regroupBiToQuad(AbstractRuleBuilder ruleBuilder,
            Variable<Set<QuadTuple<NewA, NewB, NewC, NewD>>> newSource, ViewItem collectPattern,
            ViewItem accumulatePattern) {
        ruleBuilder.applyFilterToLastPrimaryPattern(ruleBuilder.getVariables().toArray(new Variable[0]));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(ruleBuilder.getFinishedExpressions());
        newFinishedExpressions.add(accumulatePattern);
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA = Util.createVariable(ruleBuilder.generateNextId("newA"));
        Variable<NewB> newB = Util.createVariable(ruleBuilder.generateNextId("newB"));
        Variable<NewC> newC = Util.createVariable(ruleBuilder.generateNextId("newC"));
        Variable<NewD> newD = Util.createVariable(ruleBuilder.generateNextId("newD"));
        List<Variable> newVariables = Arrays.asList(newA, newB, newC, newD);
        Variable<QuadTuple<NewA, NewB, NewC, NewD>> newTuple =
                (Variable<QuadTuple<NewA, NewB, NewC, NewD>>) Util.createVariable(QuadTuple.class,
                        ruleBuilder.generateNextId("quadGrouped"), PatternDSL.from(newSource));
        PatternDef<QuadTuple<NewA, NewB, NewC, NewD>> newPrimaryPattern = PatternDSL.pattern(newTuple)
                .bind(newA, tuple -> tuple.a)
                .bind(newB, tuple -> tuple.b)
                .bind(newC, tuple -> tuple.c)
                .bind(newD, tuple -> tuple.d);
        return new QuadRuleBuilder(ruleBuilder::generateNextId, ruleBuilder.getExpectedGroupByCount(),
                newFinishedExpressions, newVariables, singletonList(newPrimaryPattern), emptyMap());
    }

    @FunctionalInterface
    protected interface Transformer<InTuple> extends
            TriFunction<Variable<Collection<InTuple>>, PatternDef<Collection<InTuple>>, ViewItem<?>, AbstractRuleBuilder> {

    }
}
