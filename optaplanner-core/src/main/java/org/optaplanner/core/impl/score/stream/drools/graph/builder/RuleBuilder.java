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

package org.optaplanner.core.impl.score.stream.drools.graph.builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ExprViewItem;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.BiConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.QuadConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.TriConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.UniConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNodeType;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.FromNode;

public final class RuleBuilder {

    public static final String VARIABLE_TYPE_RULE_METADATA_KEY = "constraintStreamVariableTypes";

    private final ConstraintGraphNode fromNode;
    private final int expectedGroupByCount;
    private List<Variable> variables = new ArrayList<>();
    private List<PatternDSL.PatternDef> primaryPatterns = new ArrayList<>(0);
    private Map<Integer, List<ExprViewItem>> dependentExpressionMap = new HashMap<>(0);
    private Predicate filterToApplyToLastPrimaryPattern = null;

    public RuleBuilder(ConstraintGraphNode previousNode, int expectedGroupByCount) {
        this.fromNode = previousNode;
        this.expectedGroupByCount = expectedGroupByCount;
        variables.add(PatternDSL.declarationOf(fromNode.getFactType(), generateNextId("var")));
        primaryPatterns.add(PatternDSL.pattern(variables.get(0)));
    }

    protected String generateNextId(String prefix) {
        return prefix + ((FromNode) fromNode).getGraph().getNextId();
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

    public Predicate getFilterToApplyToLastPrimaryPattern() {
        return filterToApplyToLastPrimaryPattern;
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

    public void setFilterToApplyToLastPrimaryPattern(Predicate filterToApplyToLastPrimaryPattern) {
        this.filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern;
    }

    private static void impactScore(Drools drools, AbstractScoreHolder scoreHolder) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext);
    }

    private static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            int impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    private static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            long impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    private static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            BigDecimal impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    public RuleBuilder join(RuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        int newCardinality = joinNode.getCardinality();
        switch (newCardinality) {
            case 2:
                return new BiJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                        .apply(this, rightSubTreeBuilder);
            case 3:
                return new TriJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                        .apply(this, rightSubTreeBuilder);
            case 4:
                return new QuadJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                        .apply(this, rightSubTreeBuilder);
            default:
                throw new UnsupportedOperationException("Unsupported stream cardinality: " + newCardinality);
        }
    }

    public RuleBuilder andThen(ConstraintGraphNode node) {
        switch (node.getType()) {
            case FILTER:
                return andThenFilter((Supplier<Predicate>) node);
            case IF_EXISTS:
            case IF_NOT_EXISTS:
                AbstractConstraintModelJoiningNode joiningNode = (AbstractConstraintModelJoiningNode) node;
                boolean shouldExist = joiningNode.getType() == ConstraintGraphNodeType.IF_EXISTS;
                switch (node.getCardinality()) {
                    case 1:
                        return new UniExistenceMutator(joiningNode, shouldExist).apply(this);
                    case 2:
                        return new BiExistenceMutator(joiningNode, shouldExist).apply(this);
                    case 3:
                        return new TriExistenceMutator(joiningNode, shouldExist).apply(this);
                    case 4:
                        return new QuadExistenceMutator(joiningNode, shouldExist).apply(this);
                    default:
                        throw new UnsupportedOperationException();
                }
            default:
                throw new UnsupportedOperationException(node.getType().toString());
        }
    }

    private RuleBuilder andThenFilter(Supplier<Predicate> predicateSupplier) {
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = predicateSupplier.get();
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(predicateSupplier.get());
        }
        return this;
    }

    private ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables) {
        ConstraintConsequence consequence = constraint.getConsequence();
        switch (consequence.getMatchWeightType()) {
            case INTEGER:
                if (consequence instanceof UniConstraintConsequence) {
                    ToIntFunction matchWeighter = ((Supplier<ToIntFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsInt(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    ToIntBiFunction matchWeighter = ((Supplier<ToIntBiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsInt(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    ToIntTriFunction matchWeighter = ((Supplier<ToIntTriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsInt(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    ToIntQuadFunction matchWeighter = ((Supplier<ToIntQuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsInt(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            case LONG:
                if (consequence instanceof UniConstraintConsequence) {
                    ToLongFunction matchWeighter = ((Supplier<ToLongFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsLong(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    ToLongBiFunction matchWeighter = ((Supplier<ToLongBiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsLong(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    ToLongTriFunction matchWeighter = ((Supplier<ToLongTriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsLong(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    ToLongQuadFunction matchWeighter = ((Supplier<ToLongQuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, matchWeighter.applyAsLong(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            case BIG_DECIMAL:
                if (consequence instanceof UniConstraintConsequence) {
                    Function matchWeighter = ((Supplier<Function>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, (BigDecimal) matchWeighter.apply(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    BiFunction matchWeighter = ((Supplier<BiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, (BigDecimal) matchWeighter.apply(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    TriFunction matchWeighter = ((Supplier<TriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, (BigDecimal) matchWeighter.apply(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    QuadFunction matchWeighter = ((Supplier<QuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder, (BigDecimal) matchWeighter.apply(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            case DEFAULT:
                if (consequence instanceof UniConstraintConsequence) {
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore((Drools) drools,
                                    (AbstractScoreHolder) scoreHolder));
                } else if (consequence instanceof BiConstraintConsequence) {
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore((Drools) drools,
                                    (AbstractScoreHolder) scoreHolder));
                } else if (consequence instanceof TriConstraintConsequence) {
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore((Drools) drools,
                                    (AbstractScoreHolder) scoreHolder));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore((Drools) drools,
                                    (AbstractScoreHolder) scoreHolder));
                } else {
                    throw new UnsupportedOperationException();
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void applyFilterToLastPrimaryPattern() {
        if (filterToApplyToLastPrimaryPattern == null) {
            return;
        }
        primaryPatterns.get(primaryPatterns.size() - 1)
                .expr(generateNextId("filter"), filterToApplyToLastPrimaryPattern::test);
        filterToApplyToLastPrimaryPattern = null;
    }

    public List<Rule> build(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint) {
        applyFilterToLastPrimaryPattern();
        List<RuleItemBuilder> ruleItemBuilderList = new ArrayList<>(0);
        for (int i = 0; i < primaryPatterns.size(); i++) {
            ruleItemBuilderList.add(primaryPatterns.get(i));
            ruleItemBuilderList.addAll(dependentExpressionMap.getOrDefault(i, Collections.emptyList()));
        }
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal,
                variables.toArray(new Variable[0]));
        ruleItemBuilderList.add(consequence);
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .metadata(VARIABLE_TYPE_RULE_METADATA_KEY, variables.stream()
                        .map(v -> v.getType().getCanonicalName())
                        .collect(Collectors.joining(",")))
                .build(ruleItemBuilderList.toArray(new RuleItemBuilder[0]));
        return Collections.singletonList(rule);
    }

}
