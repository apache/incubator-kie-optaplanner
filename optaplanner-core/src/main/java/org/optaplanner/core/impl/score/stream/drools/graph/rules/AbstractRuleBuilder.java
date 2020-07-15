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

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.drools.model.DSL.accFunction;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.Argument;
import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Rule;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractAccumulateFunction;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupByAccumulator;
import org.optaplanner.core.impl.score.stream.drools.common.FactTuple;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.FromNode;

public abstract class AbstractRuleBuilder {

    public static final String VARIABLE_TYPE_RULE_METADATA_KEY = "constraintStreamVariableTypes";
    private final UnaryOperator<String> idSupplier;
    private final int expectedGroupByCount;
    private final List<Variable> variables;
    private final List<ViewItem> finishedExpressions;
    private final List<PatternDef> primaryPatterns;
    private final Map<Integer, List<ViewItem>> dependentExpressionMap;

    protected AbstractRuleBuilder(ConstraintGraphNode fromNode, int expectedGroupByCount) {
        this(prefix -> prefix + ((FromNode) fromNode).getGraph().getNextId(), expectedGroupByCount, emptyList(),
                emptyList(), emptyList(), emptyMap());
        variables.add(PatternDSL.declarationOf(((FromNode) fromNode).getFactType(), generateNextId("var")));
        primaryPatterns.add(PatternDSL.pattern(variables.get(0)));
    }

    protected AbstractRuleBuilder(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        this.idSupplier = idSupplier;
        this.expectedGroupByCount = expectedGroupByCount;
        this.finishedExpressions = new ArrayList<>(finishedExpressions);
        this.variables = new ArrayList<>(variables);
        this.primaryPatterns = new ArrayList<>(primaryPatterns);
        this.dependentExpressionMap = new HashMap<>(dependentExpressionMap);
    }

    public static AbstractRuleBuilder from(ConstraintGraphNode node, int expectedGroupByCount) {
        return new UniRuleBuilder(node, expectedGroupByCount);
    }

    protected static void impactScore(Drools drools, AbstractScoreHolder scoreHolder) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            int impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            long impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            BigDecimal impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected String generateNextId(String prefix) {
        return idSupplier.apply(prefix);
    }

    public int getExpectedGroupByCount() {
        return expectedGroupByCount;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<PatternDef> getPrimaryPatterns() {
        return primaryPatterns;
    }

    public Map<Integer, List<ViewItem>> getDependentExpressionMap() {
        return dependentExpressionMap;
    }

    public List<ViewItem> getFinishedExpressions() {
        return finishedExpressions;
    }

    public final AbstractRuleBuilder andThen(ConstraintGraphNode node) {
        switch (node.getType()) {
            case FILTER:
                return andThenFilter(node);
            case IF_EXISTS:
            case IF_NOT_EXISTS:
                AbstractConstraintModelJoiningNode joiningNode = (AbstractConstraintModelJoiningNode) node;
                boolean shouldExist = joiningNode.getType() == ConstraintGraphNodeType.IF_EXISTS;
                return andThenExists(joiningNode, shouldExist);
            case GROUPBY_MAPPING_ONLY:
            case GROUPBY_COLLECTING_ONLY:
            case GROUPBY_MAPPING_AND_COLLECTING:
                AbstractConstraintModelGroupingNode groupingNode = (AbstractConstraintModelGroupingNode) node;
                return andThenGroupBy(groupingNode);
            default:
                throw new UnsupportedOperationException(node.getType().toString());
        }
    }

    public abstract AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode);

    protected abstract AbstractRuleBuilder andThenFilter(ConstraintGraphNode filterNode);

    protected abstract AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode,
            boolean shouldExist);

    protected abstract AbstractRuleBuilder andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode);

    protected abstract ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables);

    protected abstract void applyFilterToLastPrimaryPattern(Variable... variables);

    public List<Rule> build(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint) {
        Variable[] variableArray = variables.toArray(new Variable[0]);
        applyFilterToLastPrimaryPattern(variableArray);
        List<RuleItemBuilder> ruleItemBuilderList = new ArrayList<>(0);
        ruleItemBuilderList.addAll(finishedExpressions);
        for (int i = 0; i < primaryPatterns.size(); i++) {
            ruleItemBuilderList.add(primaryPatterns.get(i));
            ruleItemBuilderList.addAll(dependentExpressionMap.getOrDefault(i, emptyList()));
        }
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal, variableArray);
        ruleItemBuilderList.add(consequence);
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .metadata(VARIABLE_TYPE_RULE_METADATA_KEY, getExpectedJustificationTypes()
                        .map(Class::getCanonicalName)
                        .collect(Collectors.joining(",")))
                .build(ruleItemBuilderList.toArray(new RuleItemBuilder[0]));
        return singletonList(rule);
    }

    private Stream<Class> getExpectedJustificationTypes() {
        PatternDef pattern = primaryPatterns.get(primaryPatterns.size() - 1);
        Class type = pattern.getFirstVariable().getType();
        if (FactTuple.class.isAssignableFrom(type)) {
            // There is one expected constraint justification, and that is of the tuple type.
            return Stream.of(type);
        }
        // There are plenty expected constraint justifications, one for each variable.
        return variables.stream()
                .map(Argument::getType);
    }

    abstract protected int getExpectedVariableCount();

    protected abstract <InTuple> PatternDef bindTupleVariableOnFirstGrouping(PatternDef pattern,
            Variable<InTuple> tupleVariable);

    protected ViewItem<?> getInnerAccumulatePattern(PatternDef mainAccumulatePattern, List<ViewItem> dependentExpressions) {
        ViewItem[] items = Stream.concat(Stream.<ViewItem> of(mainAccumulatePattern), dependentExpressions.stream())
                .toArray(ViewItem[]::new);
        return PatternDSL.and(items[0], copyOfRange(items, 1, items.length));
    }

    protected <NewA, InTuple, OutTuple> AbstractRuleBuilder collect(
            DroolsAbstractAccumulateFunction<?, InTuple, OutTuple> accumulateFunctionBridge) {
        applyFilterToLastPrimaryPattern(getVariables().toArray(new Variable[0]));
        int patternId = primaryPatterns.size() - 1;
        PatternDef mainAccumulatePattern = primaryPatterns.get(patternId);
        List<ViewItem> dependentsExpressions = dependentExpressionMap.getOrDefault(patternId, emptyList());
        Variable baseVariable = mainAccumulatePattern.getFirstVariable();
        boolean isRegrouping = FactTuple.class.isAssignableFrom(baseVariable.getType());
        Variable<InTuple> tupleVariable;
        if (isRegrouping) {
            tupleVariable = (Variable<InTuple>) mainAccumulatePattern.getFirstVariable();
        } else {
            tupleVariable = (Variable<InTuple>) PatternDSL.declarationOf(Object.class, generateNextId("tuple"));
            mainAccumulatePattern = bindTupleVariableOnFirstGrouping(mainAccumulatePattern, tupleVariable);
        }
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(mainAccumulatePattern, dependentsExpressions);
        Variable<NewA> outputVariable = (Variable<NewA>) PatternDSL.declarationOf(Object.class, generateNextId("collected"));
        ViewItem<?> outerAccumulatePattern = DSL.accumulate(innerAccumulatePattern,
                accFunction(() -> accumulateFunctionBridge, tupleVariable).as(outputVariable));
        return recollect(outputVariable, outerAccumulatePattern);
    }

    protected <InTuple> AbstractRuleBuilder groupWithCollect(
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier) {
        return universalGroupWithCollect(invokerSupplier,
                (var, pattern, accumulate) -> regroupBi((Variable) var, (PatternDef) pattern, accumulate));
    }

    private <InTuple> AbstractRuleBuilder universalGroupWithCollect(
            Supplier<? extends DroolsAbstractGroupByAccumulator<InTuple>> invokerSupplier, Mutator<InTuple> mutator) {
        applyFilterToLastPrimaryPattern(getVariables().toArray(new Variable[0]));
        int patternId = primaryPatterns.size() - 1;
        PatternDef mainAccumulatePattern = primaryPatterns.get(patternId);
        List<ViewItem> dependentsExpressions = dependentExpressionMap.getOrDefault(patternId, emptyList());
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(mainAccumulatePattern, dependentsExpressions);
        Variable<Collection<InTuple>> tupleCollection =
                (Variable<Collection<InTuple>>) Util.createVariable(Collection.class, "tupleCollection");
        PatternDSL.PatternDef<Collection<InTuple>> pattern = pattern(tupleCollection)
                .expr("Non-empty", collection -> !collection.isEmpty(),
                        alphaIndexedBy(Integer.class, Index.ConstraintType.GREATER_THAN, -1, Collection::size, 0));
        ViewItem<?> accumulate = DSL.accumulate(innerAccumulatePattern, accFunction(invokerSupplier).as(tupleCollection));
        return mutator.apply(tupleCollection, pattern, accumulate);
    }

    protected <NewA> AbstractRuleBuilder recollect(Variable<NewA> newA, ViewItem<?> accumulatePattern) {
        List<ViewItem> newFinishedExpressions = new ArrayList<>(getFinishedExpressions());
        for (int i = 0; i < primaryPatterns.size() - 1; i++) { // The last pattern was already converted to accumulate.
            newFinishedExpressions.add(primaryPatterns.get(i));
            newFinishedExpressions.addAll(dependentExpressionMap.getOrDefault(i, emptyList()));
        }
        newFinishedExpressions.add(accumulatePattern); // The last pattern is added here.
        PatternDef<NewA> newPrimaryPattern = PatternDSL.pattern(newA);
        return new UniRuleBuilder(this::generateNextId, expectedGroupByCount, newFinishedExpressions,
                asList(newA), asList(newPrimaryPattern), emptyMap());
    }

    public <NewA> AbstractRuleBuilder regroup(Variable<Collection<NewA>> newASource,
            PatternDef<Collection<NewA>> collectPattern, ViewItem<?> accumulatePattern) {
        List<ViewItem> newFinishedExpressions = new ArrayList<>(getFinishedExpressions());
        for (int i = 0; i < primaryPatterns.size() - 1; i++) { // The last pattern was already converted to accumulate.
            newFinishedExpressions.add(primaryPatterns.get(i));
            newFinishedExpressions.addAll(dependentExpressionMap.getOrDefault(i, emptyList()));
        }
        newFinishedExpressions.add(accumulatePattern); // The last pattern is added here.
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA = (Variable<NewA>) Util.createVariable("groupKey", DSL.from(newASource));
        PatternDef<NewA> newPrimaryPattern = PatternDSL.pattern(newA);
        return new UniRuleBuilder(this::generateNextId, expectedGroupByCount, newFinishedExpressions, asList(newA),
                asList(newPrimaryPattern), emptyMap());
    }

    public <NewA, NewB> AbstractRuleBuilder regroupBi(Variable<Collection<BiTuple<NewA, NewB>>> newSource,
            PatternDef<Collection<BiTuple<NewA, NewB>>> collectPattern, ViewItem<?> accumulatePattern) {
        Variable<BiTuple<NewA, NewB>> newTuple =
                (Variable<BiTuple<NewA, NewB>>) Util.createVariable(BiTuple.class, "groupKey",
                        PatternDSL.from(newSource));
        List<ViewItem> newFinishedExpressions = new ArrayList<>(getFinishedExpressions());
        for (int i = 0; i < primaryPatterns.size() - 1; i++) { // The last pattern was already converted to accumulate.
            newFinishedExpressions.add(primaryPatterns.get(i));
            newFinishedExpressions.addAll(dependentExpressionMap.getOrDefault(i, emptyList()));
        }
        newFinishedExpressions.add(accumulatePattern); // The last pattern is added here.
        newFinishedExpressions.add(collectPattern);
        Variable<NewA> newA = Util.createVariable("newA");
        Variable<NewB> newB = Util.createVariable("newB");
        List<Variable> newVariables = Arrays.asList(newA, newB);
        PatternDef<BiTuple<NewA, NewB>> newPrimaryPattern = PatternDSL.pattern(newTuple)
                .bind(newA, tuple -> tuple.a)
                .bind(newB, tuple -> tuple.b);
        return new BiRuleBuilder(this::generateNextId, expectedGroupByCount, newFinishedExpressions, newVariables,
                asList(newPrimaryPattern), emptyMap());
    }

    @FunctionalInterface
    protected interface Mutator<InTuple> extends
            TriFunction<Variable<Collection<InTuple>>, PatternDef<Collection<InTuple>>, ViewItem<?>, AbstractRuleBuilder> {

    }

}
