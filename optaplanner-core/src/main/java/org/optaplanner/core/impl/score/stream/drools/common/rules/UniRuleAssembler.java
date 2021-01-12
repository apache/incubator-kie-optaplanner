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
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsVariableFactory;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.FromNode;

import java.math.BigDecimal;
import java.util.function.*;

final class UniRuleAssembler extends AbstractRuleAssembler<UniLeftHandSide> {

    public UniRuleAssembler(DroolsVariableFactory variableFactory, ConstraintGraphNode previousNode) {
        super(AbstractLeftHandSide.forVariable(((FromNode) previousNode).getFactType(), variableFactory));
    }

    public UniRuleAssembler(UniLeftHandSide leftHandSide) {
        super(leftHandSide);
    }

    @Override
    protected AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        AbstractBiJoiner<?, ?> joiner = (AbstractBiJoiner<?, ?>) ((AbstractConstraintModelJoiningNode) joinNode).get().get(0);
        return new BiRuleAssembler(this.leftHandSide.join(ruleAssembler.leftHandSide, joiner));
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        Predicate predicate = ((Supplier<Predicate>) filterNode).get();
        return new UniRuleAssembler(leftHandSide.filter(predicate));
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        Class<?> otherFactType = joiningNode.getOtherFactType();
        AbstractBiJoiner<?, ?>[] joiners = (AbstractBiJoiner<?, ?>[]) joiningNode.get().stream()
                .toArray(AbstractBiJoiner[]::new);
        if (shouldExist) {
            return new UniRuleAssembler(this.leftHandSide.exists(otherFactType, joiners));
        } else {
            return new UniRuleAssembler(this.leftHandSide.notExists(otherFactType, joiners));
        }
    }

    @Override
    protected GroupByMutator new0Map1CollectGroupByMutator(Object collector) {
        return new UniGroupBy0Map1CollectMutator<>((UniConstraintCollector) collector);
    }

    @Override
    protected GroupByMutator new1Map0CollectGroupByMutator(Object mapping) {
        return new UniGroupBy1Map0CollectMutator<>((Function) mapping);
    }

    @Override
    protected GroupByMutator new1Map1CollectGroupByMutator(Object mapping, Object collector) {
        return new UniGroupBy1Map1CollectMutator<>((Function) mapping, (UniConstraintCollector) collector);
    }

    @Override
    protected GroupByMutator new2Map0CollectGroupByMutator(Object mappingA, Object mappingB) {
        return new UniGroupBy2Map0CollectMutator<>((Function) mappingA, (Function) mappingB);
    }

    @Override
    protected GroupByMutator new2Map1CollectGroupByMutator(Object mappingA, Object mappingB,
            Object collectorC) {
        return new UniGroupBy2Map1CollectMutator<>((Function) mappingA, (Function) mappingB,
                (UniConstraintCollector) collectorC);
    }

    @Override
    protected GroupByMutator new2Map2CollectGroupByMutator(Object mappingA, Object mappingB, Object collectorC,
            Object collectorD) {
        return new UniGroupBy2Map2CollectMutator<>((Function) mappingA, (Function) mappingB,
                (UniConstraintCollector) collectorC, (UniConstraintCollector) collectorD);
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
                throw new UnsupportedOperationException(consequence.getMatchWeightType().toString());
        }
    }

}
