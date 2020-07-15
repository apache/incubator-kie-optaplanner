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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;

public final class UniRuleBuilder extends AbstractRuleBuilder {

    private Predicate filterToApplyToLastPrimaryPattern = null;

    public UniRuleBuilder(ConstraintGraphNode previousNode, int expectedGroupByCount) {
        super(previousNode, expectedGroupByCount);
    }

    @Override
    public AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        return new BiJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, rightSubTreeBuilder);
    }

    @Override
    protected AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new UniExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleBuilder andThenFilter(ConstraintGraphNode filterNode) {
        Supplier<Predicate> predicateSupplier = (Supplier<Predicate>) filterNode;
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
                ToIntFunction intMatchWeighter = ((Supplier<ToIntFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0])
                        .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, intMatchWeighter.applyAsInt(a)));
            case LONG:
                ToLongFunction longMatchWeighter = ((Supplier<ToLongFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0])
                        .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, longMatchWeighter.applyAsLong(a)));
            case BIG_DECIMAL:
                Function bigDecimalMatchWeighter = ((Supplier<Function>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0])
                        .execute((drools, scoreHolder, a) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, (BigDecimal) bigDecimalMatchWeighter.apply(a)));
            case DEFAULT:
                return DSL.on(scoreHolderGlobal, variables[0])
                        .execute((drools, scoreHolder, a) -> impactScore((Drools) drools,
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
        Predicate predicate = filterToApplyToLastPrimaryPattern;
        getPrimaryPatterns().get(0)
                .expr("Filter using " + predicate, variables[0], (fact, a) -> predicate.test(a));
        filterToApplyToLastPrimaryPattern = null;
    }

}
