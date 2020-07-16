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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.Argument;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Rule;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.FactTuple;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.FromNode;

abstract class AbstractRuleAssembler implements RuleAssembler {

    private final UnaryOperator<String> idSupplier;
    private final int expectedGroupByCount;
    private final List<Variable> variables;
    private final List<ViewItem> finishedExpressions;
    private final List<PatternDef> primaryPatterns;
    private final Map<Integer, List<ViewItem>> dependentExpressionMap;

    protected AbstractRuleAssembler(ConstraintGraphNode fromNode, int expectedGroupByCount) {
        this(prefix -> prefix + ((FromNode) fromNode).getGraph().getNextId(), expectedGroupByCount, emptyList(),
                emptyList(), emptyList(), emptyMap());
        variables.add(PatternDSL.declarationOf(((FromNode) fromNode).getFactType(), generateNextId("var")));
        primaryPatterns.add(PatternDSL.pattern(variables.get(0)));
    }

    protected AbstractRuleAssembler(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        this.idSupplier = idSupplier;
        this.expectedGroupByCount = expectedGroupByCount;
        this.finishedExpressions = new ArrayList<>(finishedExpressions);
        this.variables = new ArrayList<>(variables);
        this.primaryPatterns = new ArrayList<>(primaryPatterns);
        this.dependentExpressionMap = new HashMap<>(dependentExpressionMap);
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

    String generateNextId(String prefix) {
        return idSupplier.apply(prefix);
    }

    int getExpectedGroupByCount() {
        return expectedGroupByCount;
    }

    List<Variable> getVariables() {
        return variables;
    }

    List<PatternDef> getPrimaryPatterns() {
        return primaryPatterns;
    }

    Map<Integer, List<ViewItem>> getDependentExpressionMap() {
        return dependentExpressionMap;
    }

    List<ViewItem> getFinishedExpressions() {
        return finishedExpressions;
    }

    @Override
    public final AbstractRuleAssembler andThen(ConstraintGraphNode node) {
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

    @Override
    public final RuleAssembler join(RuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        return join((AbstractRuleAssembler) ruleAssembler, joinNode);
    }

    protected abstract AbstractRuleAssembler join(AbstractRuleAssembler ruleAssembler, ConstraintGraphNode joinNode);

    protected abstract AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode);

    protected abstract AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode,
            boolean shouldExist);

    protected abstract AbstractRuleAssembler andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode);

    protected abstract ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables);

    protected abstract void applyFilterToLastPrimaryPattern(Variable... variables);

    public List<Rule> assemble(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint) {
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

}
