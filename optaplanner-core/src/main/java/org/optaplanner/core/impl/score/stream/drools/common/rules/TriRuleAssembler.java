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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

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
import org.optaplanner.core.api.function.ToIntTriFunction;
import org.optaplanner.core.api.function.ToLongTriFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;

final class TriRuleAssembler extends AbstractRuleAssembler<TriPredicate> {

    private TriPredicate filterToApplyToLastPrimaryPattern = null;

    public TriRuleAssembler(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        super(idSupplier, expectedGroupByCount, finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

    @Override
    protected void addFilterToLastPrimaryPattern(TriPredicate triPredicate) {
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = triPredicate;
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(triPredicate);
        }
    }

    @Override
    protected AbstractRuleAssembler join(UniRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        return new QuadJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, ruleAssembler);
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new TriExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleAssembler andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        List<TriFunction> mappings = groupingNode.getMappings();
        int mappingCount = mappings.size();
        List<TriConstraintCollector> collectors = groupingNode.getCollectors();
        int collectorCount = collectors.size();
        switch (groupingNode.getType()) {
            case GROUPBY_MAPPING_ONLY:
                switch (mappingCount) {
                    case 1:
                        return new TriGroupBy1Map0CollectMutator<>(mappings.get(0)).apply(this);
                    case 2:
                        return new TriGroupBy2Map0CollectMutator<>(mappings.get(0), mappings.get(1)).apply(this);
                    default:
                        throw new IllegalStateException("Invalid number of mappings: " + mappingCount);
                }
            case GROUPBY_COLLECTING_ONLY:
                if (collectorCount == 1) {
                    return new TriGroupBy0Map1CollectMutator<>(collectors.get(0)).apply(this);
                }
                throw new IllegalStateException("Invalid number of collectors: " + collectorCount);
            case GROUPBY_MAPPING_AND_COLLECTING:
                if (mappingCount == 1 && collectorCount == 1) {
                    return new TriGroupBy1Map1CollectMutator<>(mappings.get(0), collectors.get(0)).apply(this);
                } else if (mappingCount == 2 && collectorCount == 1) {
                    return new TriGroupBy2Map1CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0))
                            .apply(this);
                } else if (mappingCount == 2 && collectorCount == 2) {
                    return new TriGroupBy2Map2CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0),
                            collectors.get(1)).apply(this);
                } else {
                    throw new IllegalStateException(
                            "Invalid number of mappings (" + mappingCount + ") and collectors (" + collectorCount + ").");
                }
            default:
                throw new UnsupportedOperationException();
        }
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
    protected void applyFilterToLastPrimaryPattern() {
        if (filterToApplyToLastPrimaryPattern == null) {
            return;
        }
        TriPredicate predicate = filterToApplyToLastPrimaryPattern;
        getLastPrimaryPattern()
                .expr("Filter using " + predicate, getVariable(0), getVariable(1), getVariable(2),
                        (fact, a, b, c) -> predicate.test(a, b, c));
        filterToApplyToLastPrimaryPattern = null;
    }

}
