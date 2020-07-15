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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;

final class QuadRuleBuilder extends AbstractRuleBuilder {

    private QuadPredicate filterToApplyToLastPrimaryPattern = null;

    public QuadRuleBuilder(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        super(idSupplier, expectedGroupByCount, finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

    @Override
    public AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        throw new UnsupportedOperationException("Penta streams are not supported.");
    }

    @Override
    protected AbstractRuleBuilder andThenFilter(ConstraintGraphNode filterNode) {
        Supplier<QuadPredicate> predicateSupplier = (Supplier<QuadPredicate>) filterNode;
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = predicateSupplier.get();
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(predicateSupplier.get());
        }
        return this;
    }

    @Override
    protected AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new QuadExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleBuilder andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables) {
        ConstraintConsequence consequence = constraint.getConsequence();
        switch (consequence.getMatchWeightType()) {
            case INTEGER:
                ToIntQuadFunction intMatchWeighter = ((Supplier<ToIntQuadFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                        .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, intMatchWeighter.applyAsInt(a, b, c, d)));
            case LONG:
                ToLongQuadFunction longMatchWeighter = ((Supplier<ToLongQuadFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                        .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, longMatchWeighter.applyAsLong(a, b, c, d)));
            case BIG_DECIMAL:
                QuadFunction bigDecimalMatchWeighter = ((Supplier<QuadFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                        .execute((drools, scoreHolder, a, b, c, d) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, (BigDecimal) bigDecimalMatchWeighter.apply(a, b, c, d)));
            case DEFAULT:
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2], variables[3])
                        .execute((drools, scoreHolder, a, b, c, d) -> impactScore((Drools) drools,
                                (AbstractScoreHolder) scoreHolder));
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    protected void applyFilterToLastPrimaryPattern(Variable... variables) {
        if (filterToApplyToLastPrimaryPattern == null) {
            return;
        }
        QuadPredicate predicate = filterToApplyToLastPrimaryPattern;
        getPrimaryPatterns().get(getPrimaryPatterns().size() - 1)
                .expr("Filter using " + predicate, variables[0], variables[1], variables[2], variables[3],
                        (fact, a, b, c, d) -> predicate.test(a, b, c, d));
        filterToApplyToLastPrimaryPattern = null;
    }

}
