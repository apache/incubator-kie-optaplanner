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
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
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
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;

public final class RuleBuilder {

    public static final String VARIABLE_TYPE_RULE_METADATA_KEY = "constraintStreamVariableTypes";

    private final ConstraintGraphNode fromNode;
    private Predicate filterToApply = null;

    public RuleBuilder(ConstraintGraphNode previousNode, int expectedGroupByCount) {
        this.fromNode = previousNode;
    }

    public RuleBuilder join(RuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        throw new UnsupportedOperationException("JOIN");
    }

    public RuleBuilder andThen(ConstraintGraphNode node) {
        switch (node.getType()) {
            case FILTER:
                return andThenFilter((Supplier<Predicate>) node);
            default:
                throw new UnsupportedOperationException(node.getType().toString());
        }
    }

    private RuleBuilder andThenFilter(Supplier<Predicate> predicateSupplier) {
        if (filterToApply == null) {
            filterToApply = predicateSupplier.get();
        } else {
            filterToApply = filterToApply.and(predicateSupplier.get());
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
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsInt(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    ToIntBiFunction matchWeighter = ((Supplier<ToIntBiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsInt(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    ToIntTriFunction matchWeighter = ((Supplier<ToIntTriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsInt(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    ToIntQuadFunction matchWeighter = ((Supplier<ToIntQuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsInt(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            case LONG:
                if (consequence instanceof UniConstraintConsequence) {
                    ToLongFunction matchWeighter = ((Supplier<ToLongFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsLong(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    ToLongBiFunction matchWeighter = ((Supplier<ToLongBiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsLong(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    ToLongTriFunction matchWeighter = ((Supplier<ToLongTriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsLong(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    ToLongQuadFunction matchWeighter = ((Supplier<ToLongQuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    matchWeighter.applyAsLong(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            case BIG_DECIMAL:
                if (consequence instanceof UniConstraintConsequence) {
                    Function matchWeighter = ((Supplier<Function>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0])
                            .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    (BigDecimal) matchWeighter.apply(a)));
                } else if (consequence instanceof BiConstraintConsequence) {
                    BiFunction matchWeighter = ((Supplier<BiFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                            .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    (BigDecimal) matchWeighter.apply(a, b)));
                } else if (consequence instanceof TriConstraintConsequence) {
                    TriFunction matchWeighter = ((Supplier<TriFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                            .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    (BigDecimal) matchWeighter.apply(a, b, c)));
                } else if (consequence instanceof QuadConstraintConsequence) {
                    QuadFunction matchWeighter = ((Supplier<QuadFunction>) consequence).get();
                    return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                            .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                    (AbstractScoreHolder) scoreHolder,
                                    (BigDecimal) matchWeighter.apply(a, b, c, d)));
                } else {
                    throw new UnsupportedOperationException();
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    public <A> List<Rule> build(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            DroolsConstraint constraint) {
        Variable<A> aVar = PatternDSL.declarationOf(fromNode.getFactType(), "aVar");
        PatternDSL.PatternDef<A> aPattern = PatternDSL.pattern(aVar);
        if (filterToApply != null) {
            aPattern = aPattern.expr("aFilter", filterToApply::test);
        }
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal, aVar);
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .metadata(VARIABLE_TYPE_RULE_METADATA_KEY, aVar.getType().getCanonicalName())
                .build(aPattern, consequence);
        return Collections.singletonList(rule);
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

}
