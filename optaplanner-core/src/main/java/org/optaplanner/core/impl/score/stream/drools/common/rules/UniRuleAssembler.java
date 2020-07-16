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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;

final class UniRuleAssembler extends AbstractRuleAssembler {

    private Predicate filterToApplyToLastPrimaryPattern = null;

    public UniRuleAssembler(ConstraintGraphNode previousNode, int expectedGroupByCount) {
        super(previousNode, expectedGroupByCount);
    }

    public UniRuleAssembler(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        super(idSupplier, expectedGroupByCount, finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

    @Override
    protected AbstractRuleAssembler join(AbstractRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        return new BiJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, ruleAssembler);
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        Supplier<Predicate> predicateSupplier = (Supplier<Predicate>) filterNode;
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = predicateSupplier.get();
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(predicateSupplier.get());
        }
        return this;
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new UniExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleAssembler andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        List<Function> mappings = groupingNode.getMappings();
        int mappingCount = mappings.size();
        List<UniConstraintCollector> collectors = groupingNode.getCollectors();
        int collectorCount = collectors.size();
        switch (groupingNode.getType()) {
            case GROUPBY_MAPPING_ONLY:
                switch (mappingCount) {
                    case 1:
                        return new UniGroupBy1Map0CollectMutator<>(mappings.get(0)).apply(this);
                    case 2:
                        return new UniGroupBy2Map0CollectMutator<>(mappings.get(0), mappings.get(1)).apply(this);
                    default:
                        throw new IllegalStateException("Invalid number of mappings: " + mappingCount);
                }
            case GROUPBY_COLLECTING_ONLY:
                if (collectorCount == 1) {
                    return new UniGroupBy0Map1CollectMutator<>(collectors.get(0)).apply(this);
                }
                throw new IllegalStateException("Invalid number of collectors: " + collectorCount);
            case GROUPBY_MAPPING_AND_COLLECTING:
                if (mappingCount == 1 && collectorCount == 1) {
                    return new UniGroupBy1Map1CollectMutator<>(mappings.get(0), collectors.get(0)).apply(this);
                } else if (mappingCount == 2 && collectorCount == 1) {
                    return new UniGroupBy2Map1CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0))
                            .apply(this);
                } else if (mappingCount == 2 && collectorCount == 2) {
                    return new UniGroupBy2Map2CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0),
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
        getPrimaryPatterns().get(getPrimaryPatterns().size() - 1)
                .expr("Filter using " + predicate, variables[0], (fact, a) -> predicate.test(a));
        filterToApplyToLastPrimaryPattern = null;
    }

}
