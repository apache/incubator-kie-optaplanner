/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.quad.AbstractQuadJoiner;

import java.math.BigDecimal;
import java.util.function.Supplier;

final class TriRuleAssembler extends AbstractRuleAssembler<TriLeftHandSide> {

    public TriRuleAssembler(TriLeftHandSide leftHandSide) {
        super(leftHandSide);
    }

    @Override
    protected AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        AbstractQuadJoiner<?, ?, ?, ?> joiner =
                (AbstractQuadJoiner<?, ?, ?, ?>) ((AbstractConstraintModelJoiningNode) joinNode).get().get(0);
        return new QuadRuleAssembler(this.leftHandSide.join(ruleAssembler.leftHandSide, joiner));
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        TriPredicate predicate = ((Supplier<TriPredicate>) filterNode).get();
        return new TriRuleAssembler(leftHandSide.filter(predicate));
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        Class<?> otherFactType =  joiningNode.getOtherFactType();
        AbstractQuadJoiner<?, ?, ?, ?>[] joiners = (AbstractQuadJoiner<?, ?, ?, ?>[]) joiningNode.get().stream()
                .toArray(AbstractQuadJoiner[]::new);
        if (shouldExist) {
            return new TriRuleAssembler(this.leftHandSide.exists(otherFactType, joiners));
        } else {
            return new TriRuleAssembler(this.leftHandSide.notExists(otherFactType, joiners));
        }
    }

    @Override
    protected UniRuleAssembler andThenGroupBy0Map1Collect(Object collector) {
        return (UniRuleAssembler) new TriGroupBy0Map1CollectMutator<>((TriConstraintCollector) collector).apply(this);
    }

    @Override
    protected UniRuleAssembler andThenGroupBy1Map0Collect(Object mapping) {
        return (UniRuleAssembler) new TriGroupBy1Map0CollectMutator<>((TriFunction) mapping).apply(this);
    }

    @Override
    protected BiRuleAssembler andThenGroupBy1Map1Collect(Object mapping, Object collector) {
        return (BiRuleAssembler) new TriGroupBy1Map1CollectMutator<>((TriFunction) mapping, (TriConstraintCollector) collector).apply(this);
    }

    @Override
    protected BiRuleAssembler andThenGroupBy2Map0Collect(Object mappingA, Object mappingB) {
        return (BiRuleAssembler) new TriGroupBy2Map0CollectMutator<>((TriFunction) mappingA, (TriFunction) mappingB).apply(this);
    }

    @Override
    protected TriRuleAssembler andThenGroupBy2Map1Collect(Object mappingA, Object mappingB,
                                                        Object collectorC) {
        return (TriRuleAssembler) new TriGroupBy2Map1CollectMutator<>((TriFunction) mappingA, (TriFunction) mappingB,
                (TriConstraintCollector) collectorC).apply(this);
    }

    @Override
    protected QuadRuleAssembler andThenGroupBy2Map2Collect(Object mappingA, Object mappingB, Object collectorC,
                                                        Object collectorD) {
        return (QuadRuleAssembler) new TriGroupBy2Map2CollectMutator<>((TriFunction) mappingA, (TriFunction) mappingB,
                (TriConstraintCollector) collectorC, (TriConstraintCollector) collectorD).apply(this);
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
                throw new UnsupportedOperationException(consequence.getMatchWeightType().toString());
        }
    }

}
