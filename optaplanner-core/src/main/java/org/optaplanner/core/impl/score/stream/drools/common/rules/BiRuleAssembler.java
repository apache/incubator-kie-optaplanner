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
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

import java.math.BigDecimal;
import java.util.function.*;

final class BiRuleAssembler extends AbstractRuleAssembler<BiLeftHandSide> {

    public BiRuleAssembler(BiLeftHandSide leftHandSide) {
        super(leftHandSide);
    }

    @Override
    protected AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        AbstractTriJoiner<?, ?, ?> joiner =
                (AbstractTriJoiner<?, ?, ?>) ((AbstractConstraintModelJoiningNode) joinNode).get().get(0);
        return new TriRuleAssembler(this.leftHandSide.join(ruleAssembler.leftHandSide, joiner));
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        BiPredicate predicate = ((Supplier<BiPredicate>) filterNode).get();
        return new BiRuleAssembler(leftHandSide.filter(predicate));
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        Class<?> otherFactType = joiningNode.getOtherFactType();
        AbstractTriJoiner<?, ?, ?>[] joiners = (AbstractTriJoiner<?, ?, ?>[]) joiningNode.get().stream()
                .toArray(AbstractTriJoiner[]::new);
        if (shouldExist) {
            return new BiRuleAssembler(this.leftHandSide.exists(otherFactType, joiners));
        } else {
            return new BiRuleAssembler(this.leftHandSide.notExists(otherFactType, joiners));
        }
    }

    @Override
    protected GroupByMutator new0Map1CollectGroupByMutator(Object collector) {
        return new BiGroupBy0Map1CollectMutator<>((BiConstraintCollector) collector);
    }

    @Override
    protected GroupByMutator new1Map0CollectGroupByMutator(Object mapping) {
        return new BiGroupBy1Map0CollectMutator<>((BiFunction) mapping);
    }

    @Override
    protected GroupByMutator new1Map1CollectGroupByMutator(Object mapping, Object collector) {
        return new BiGroupBy1Map1CollectMutator<>((BiFunction) mapping, (BiConstraintCollector) collector);
    }

    @Override
    protected GroupByMutator new2Map0CollectGroupByMutator(Object mappingA, Object mappingB) {
        return new BiGroupBy2Map0CollectMutator<>((BiFunction) mappingA, (BiFunction) mappingB);
    }

    @Override
    protected GroupByMutator new2Map1CollectGroupByMutator(Object mappingA, Object mappingB,
            Object collectorC) {
        return new BiGroupBy2Map1CollectMutator<>((BiFunction) mappingA, (BiFunction) mappingB,
                (BiConstraintCollector) collectorC);
    }

    @Override
    protected GroupByMutator new2Map2CollectGroupByMutator(Object mappingA, Object mappingB, Object collectorC,
            Object collectorD) {
        return new BiGroupBy2Map2CollectMutator<>((BiFunction) mappingA, (BiFunction) mappingB,
                (BiConstraintCollector) collectorC, (BiConstraintCollector) collectorD);
    }

    @Override
    protected ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables) {
        ConstraintConsequence consequence = constraint.getConsequence();
        switch (consequence.getMatchWeightType()) {
            case INTEGER:
                ToIntBiFunction intMatchWeighter = ((Supplier<ToIntBiFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                        .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, intMatchWeighter.applyAsInt(a, b)));
            case LONG:
                ToLongBiFunction longMatchWeighter = ((Supplier<ToLongBiFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                        .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, longMatchWeighter.applyAsLong(a, b)));
            case BIG_DECIMAL:
                BiFunction bigDecimalMatchWeighter = ((Supplier<BiFunction>) consequence).get();
                return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                        .execute((drools, scoreHolder, a, b) -> impactScore(constraint, (Drools) drools,
                                (AbstractScoreHolder) scoreHolder, (BigDecimal) bigDecimalMatchWeighter.apply(a, b)));
            case DEFAULT:
                return DSL.on(scoreHolderGlobal, variables[0], variables[1])
                        .execute((drools, scoreHolder, a, b) -> impactScore((Drools) drools,
                                (AbstractScoreHolder) scoreHolder));
            default:
                throw new UnsupportedOperationException(consequence.getMatchWeightType().toString());
        }
    }

}
