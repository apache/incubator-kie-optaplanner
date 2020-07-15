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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;

final class TriRuleBuilder extends AbstractRuleBuilder {

    private TriPredicate filterToApplyToLastPrimaryPattern = null;

    public TriRuleBuilder(UnaryOperator<String> idSupplier, int expectedGroupByCount) {
        super(idSupplier, expectedGroupByCount);
    }

    @Override
    public AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        return new QuadJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, rightSubTreeBuilder);
    }

    @Override
    protected AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new TriExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleBuilder andThenFilter(ConstraintGraphNode filterNode) {
        Supplier<TriPredicate> predicateSupplier = (Supplier<TriPredicate>) filterNode;
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = predicateSupplier.get();
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(predicateSupplier.get());
        }
        return this;
    }

    @Override
    protected ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables) {
        ConstraintConsequence consequence = constraint.getConsequence();
        switch (consequence.getMatchWeightType()) {
            case INTEGER:
                ToIntTriFunction intMatchWeighter = ((Supplier<ToIntTriFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, intMatchWeighter.applyAsInt(a, b, c)));
            case LONG:
                ToLongTriFunction longMatchWeighter = ((Supplier<ToLongTriFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, longMatchWeighter.applyAsLong(a, b, c)));
            case BIG_DECIMAL:
                TriFunction bigDecimalMatchWeighter = ((Supplier<TriFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                        .execute((drools, scoreHolder, a, b, c) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, (BigDecimal) bigDecimalMatchWeighter.apply(a, b, c)));
            case DEFAULT:
                return DSL.on(scoreHolderGlobal, variables[0], variables[1], variables[2])
                        .execute((drools, scoreHolder, a, b, c) -> impactScore((Drools) drools,
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
        TriPredicate predicate = filterToApplyToLastPrimaryPattern;
        getPrimaryPatterns().get(2).expr("Filter using " + predicate, variables[0], variables[1], variables[2],
                (fact, a, b, c) -> predicate.test(a, b, c));
        filterToApplyToLastPrimaryPattern = null;
    }

}
