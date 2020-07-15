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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ExprViewItem;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.FromNode;

public abstract class AbstractRuleBuilder {

    public static final String VARIABLE_TYPE_RULE_METADATA_KEY = "constraintStreamVariableTypes";

    public static AbstractRuleBuilder from(ConstraintGraphNode node, int expectedGroupByCount) {
        return new UniRuleBuilder(node, expectedGroupByCount);
    }

    private final UnaryOperator<String> idSupplier;
    private final int expectedGroupByCount;
    private List<Variable> variables = new ArrayList<>();
    private List<PatternDSL.PatternDef> primaryPatterns = new ArrayList<>(0);
    private Map<Integer, List<ExprViewItem>> dependentExpressionMap = new HashMap<>(0);

    protected AbstractRuleBuilder(ConstraintGraphNode fromNode, int expectedGroupByCount) {
        this.idSupplier = prefix -> prefix + ((FromNode) fromNode).getGraph().getNextId();
        this.expectedGroupByCount = expectedGroupByCount;
        variables.add(PatternDSL.declarationOf(((FromNode) fromNode).getFactType(), generateNextId("var")));
        primaryPatterns.add(PatternDSL.pattern(variables.get(0)));
    }

    protected AbstractRuleBuilder(UnaryOperator<String> idSupplier, int expectedGroupByCount) {
        this.idSupplier = idSupplier;
        this.expectedGroupByCount = expectedGroupByCount;
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

    public List<PatternDSL.PatternDef> getPrimaryPatterns() {
        return primaryPatterns;
    }

    public Map<Integer, List<ExprViewItem>> getDependentExpressionMap() {
        return dependentExpressionMap;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void setPrimaryPatterns(List<PatternDSL.PatternDef> primaryPatterns) {
        this.primaryPatterns = primaryPatterns;
    }

    public void setDependentExpressionMap(Map<Integer, List<ExprViewItem>> dependentExpressionMap) {
        this.dependentExpressionMap = dependentExpressionMap;
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

    public final AbstractRuleBuilder andThen(ConstraintGraphNode node) {
        switch (node.getType()) {
            case FILTER:
                return andThenFilter(node);
            case IF_EXISTS:
            case IF_NOT_EXISTS:
                AbstractConstraintModelJoiningNode joiningNode = (AbstractConstraintModelJoiningNode) node;
                boolean shouldExist = joiningNode.getType() == ConstraintGraphNodeType.IF_EXISTS;
                return andThenExists(joiningNode, shouldExist);
            default:
                throw new UnsupportedOperationException(node.getType().toString());
        }
    }

    public abstract AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode);

    protected abstract AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode,
            boolean shouldExist);

    protected abstract AbstractRuleBuilder andThenFilter(ConstraintGraphNode filterNode);

    protected abstract ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables);

    protected abstract void applyFilterToLastPrimaryPattern(Variable... variables);

    public List<Rule> build(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint) {
        Variable[] variableArray = variables.toArray(new Variable[0]);
        applyFilterToLastPrimaryPattern(variableArray);
        List<RuleItemBuilder> ruleItemBuilderList = new ArrayList<>(0);
        for (int i = 0; i < primaryPatterns.size(); i++) {
            ruleItemBuilderList.add(primaryPatterns.get(i));
            ruleItemBuilderList.addAll(dependentExpressionMap.getOrDefault(i, Collections.emptyList()));
        }
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal, variableArray);
        ruleItemBuilderList.add(consequence);
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .metadata(VARIABLE_TYPE_RULE_METADATA_KEY, variables.stream()
                        .map(v -> v.getType().getCanonicalName())
                        .collect(Collectors.joining(",")))
                .build(ruleItemBuilderList.toArray(new RuleItemBuilder[0]));
        return Collections.singletonList(rule);
    }

}
