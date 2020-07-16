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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.UnaryOperator;

import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;

final class BiRuleAssembler extends AbstractRuleAssembler {

    private BiPredicate filterToApplyToLastPrimaryPattern = null;

    public BiRuleAssembler(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        super(idSupplier, expectedGroupByCount, finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

    @Override
    protected AbstractRuleAssembler join(AbstractRuleAssembler ruleAssembler, ConstraintGraphNode joinNode) {
        return new TriJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, ruleAssembler);
    }

    @Override
    protected AbstractRuleAssembler andThenFilter(ConstraintGraphNode filterNode) {
        Supplier<BiPredicate> predicateSupplier = (Supplier<BiPredicate>) filterNode;
        if (filterToApplyToLastPrimaryPattern == null) {
            filterToApplyToLastPrimaryPattern = predicateSupplier.get();
        } else {
            filterToApplyToLastPrimaryPattern = filterToApplyToLastPrimaryPattern.and(predicateSupplier.get());
        }
        return this;
    }

    @Override
    protected AbstractRuleAssembler andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new BiExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleAssembler andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        List<BiFunction> mappings = groupingNode.getMappings();
        int mappingCount = mappings.size();
        List<BiConstraintCollector> collectors = groupingNode.getCollectors();
        int collectorCount = collectors.size();
        switch (groupingNode.getType()) {
            case GROUPBY_MAPPING_ONLY:
                switch (mappingCount) {
                    case 1:
                        return new BiGroupBy1Map0CollectMutator<>(mappings.get(0)).apply(this);
                    case 2:
                        return new BiGroupBy2Map0CollectMutator<>(mappings.get(0), mappings.get(1)).apply(this);
                    default:
                        throw new IllegalStateException("Invalid number of mappings: " + mappingCount);
                }
            case GROUPBY_COLLECTING_ONLY:
                if (collectorCount == 1) {
                    return new BiGroupBy0Map1CollectMutator<>(collectors.get(0)).apply(this);
                }
                throw new IllegalStateException("Invalid number of collectors: " + collectorCount);
            case GROUPBY_MAPPING_AND_COLLECTING:
                if (mappingCount == 1 && collectorCount == 1) {
                    return new BiGroupBy1Map1CollectMutator<>(mappings.get(0), collectors.get(0)).apply(this);
                } else if (mappingCount == 2 && collectorCount == 1) {
                    return new BiGroupBy2Map1CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0))
                            .apply(this);
                } else if (mappingCount == 2 && collectorCount == 2) {
                    return new BiGroupBy2Map2CollectMutator<>(mappings.get(0), mappings.get(1), collectors.get(0),
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
                throw new UnsupportedOperationException();
        }
    }

    @Override
    protected void applyFilterToLastPrimaryPattern(Variable... variables) {
        if (filterToApplyToLastPrimaryPattern == null) {
            return;
        }
        BiPredicate predicate = filterToApplyToLastPrimaryPattern;
        getPrimaryPatterns().get(getPrimaryPatterns().size() - 1)
                .expr("Filter using " + predicate, variables[0], variables[1],
                        (fact, a, b) -> predicate.test(a, b));
        filterToApplyToLastPrimaryPattern = null;
    }

}
