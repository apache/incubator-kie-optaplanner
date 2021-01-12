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
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.ToIntQuadFunction;
import org.optaplanner.core.api.function.ToLongQuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.penta.AbstractPentaJoiner;

import java.math.BigDecimal;
import java.util.function.Supplier;

final class QuadRuleAssembler extends AbstractRuleAssembler<QuadLeftHandSide> {

    public QuadRuleAssembler(QuadLeftHandSide leftHandSide) {
        super(leftHandSide);
    }

    @Override
    protected AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        throw new UnsupportedOperationException("Impossible state: Penta streams are not supported.");
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        QuadPredicate predicate = ((Supplier<QuadPredicate>) filterNode).get();
        return new QuadRuleAssembler(leftHandSide.filter(predicate));
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        Class<?> otherFactType =  joiningNode.getOtherFactType();
        AbstractPentaJoiner<?, ?, ?, ?, ?>[] joiners = (AbstractPentaJoiner<?, ?, ?, ?, ?>[]) joiningNode.get().stream()
                .toArray(AbstractPentaJoiner[]::new);
        if (shouldExist) {
            return new QuadRuleAssembler(this.leftHandSide.exists(otherFactType, joiners));
        } else {
            return new QuadRuleAssembler(this.leftHandSide.notExists(otherFactType, joiners));
        }
    }

    @Override
    protected UniRuleAssembler andThenGroupBy0Map1Collect(Object collector) {
        return (UniRuleAssembler) new QuadGroupBy0Map1CollectMutator<>((QuadConstraintCollector) collector).apply(this);
    }

    @Override
    protected UniRuleAssembler andThenGroupBy1Map0Collect(Object mapping) {
        return (UniRuleAssembler) new QuadGroupBy1Map0CollectMutator<>((QuadFunction) mapping).apply(this);
    }

    @Override
    protected BiRuleAssembler andThenGroupBy1Map1Collect(Object mapping, Object collector) {
        return (BiRuleAssembler) new QuadGroupBy1Map1CollectMutator<>((QuadFunction) mapping, (QuadConstraintCollector) collector).apply(this);
    }

    @Override
    protected BiRuleAssembler andThenGroupBy2Map0Collect(Object mappingA, Object mappingB) {
        return (BiRuleAssembler) new QuadGroupBy2Map0CollectMutator<>((QuadFunction) mappingA, (QuadFunction) mappingB).apply(this);
    }

    @Override
    protected TriRuleAssembler andThenGroupBy2Map1Collect(Object mappingA, Object mappingB,
                                                        Object collectorC) {
        return (TriRuleAssembler) new QuadGroupBy2Map1CollectMutator<>((QuadFunction) mappingA, (QuadFunction) mappingB,
                (QuadConstraintCollector) collectorC).apply(this);
    }

    @Override
    protected QuadRuleAssembler andThenGroupBy2Map2Collect(Object mappingA, Object mappingB, Object collectorC,
                                                        Object collectorD) {
        return (QuadRuleAssembler) new QuadGroupBy2Map2CollectMutator<>((QuadFunction) mappingA, (QuadFunction) mappingB,
                (QuadConstraintCollector) collectorC, (QuadConstraintCollector) collectorD).apply(this);
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
                throw new UnsupportedOperationException(consequence.getMatchWeightType().toString());
        }
    }

}
