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

import static org.drools.model.DSL.accFunction;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.declarationOf;
import static org.drools.model.PatternDSL.pattern;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import org.drools.core.base.accumulators.CollectSetAccumulateFunction;
import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.PatternDSL.PatternDef;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.graph.consequences.ConstraintConsequence;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelGroupingNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.AbstractConstraintModelJoiningNode;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniAccumulateFunction;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniToBiGroupByAccumulator;

public final class UniRuleBuilder extends AbstractRuleBuilder {

    private Predicate filterToApplyToLastPrimaryPattern = null;

    public UniRuleBuilder(ConstraintGraphNode previousNode, int expectedGroupByCount) {
        super(previousNode, expectedGroupByCount);
    }

    public UniRuleBuilder(UnaryOperator<String> idSupplier, int expectedGroupByCount,
            List<ViewItem> finishedExpressions, List<Variable> variables, List<PatternDef> primaryPatterns,
            Map<Integer, List<ViewItem>> dependentExpressionMap) {
        super(idSupplier, expectedGroupByCount, finishedExpressions, variables, primaryPatterns, dependentExpressionMap);
    }

    @Override
    public AbstractRuleBuilder join(AbstractRuleBuilder rightSubTreeBuilder, ConstraintGraphNode joinNode) {
        return new BiJoinMutator<>((AbstractConstraintModelJoiningNode) joinNode)
                .apply(this, rightSubTreeBuilder);
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
    protected AbstractRuleBuilder andThenExists(AbstractConstraintModelJoiningNode joiningNode, boolean shouldExist) {
        return new UniExistenceMutator(joiningNode, shouldExist).apply(this);
    }

    @Override
    protected AbstractRuleBuilder andThenGroupBy(AbstractConstraintModelGroupingNode groupingNode) {
        List<Function> mappings = groupingNode.getMappings();
        int mappingCount = mappings.size();
        List<UniConstraintCollector> collectors = groupingNode.getCollectors();
        int collectorCount = collectors.size();
        switch (groupingNode.getType()) {
            case GROUPBY_MAPPING_ONLY:
                switch (mappingCount) {
                    case 1:
                        return map(mappings.get(0));
                    case 2:
                        return map(mappings.get(0), mappings.get(1));
                    default:
                        throw new IllegalStateException("Invalid number of mappings: " + mappingCount);
                }
            case GROUPBY_COLLECTING_ONLY:
                switch (collectorCount) {
                    case 1:
                        return collect(collectors.get(0));
                    default:
                        throw new IllegalStateException("Invalid number of collectors: " + collectorCount);
                }
            case GROUPBY_MAPPING_AND_COLLECTING:
                if (mappingCount == 1 && collectorCount == 1) {
                    return mapAndCollect(mappings.get(0), collectors.get(0));
                } else if (mappingCount == 2 && collectorCount == 1) {
                    return mapAndCollect(mappings.get(0), mappings.get(1), collectors.get(0));
                } else if (mappingCount == 2 && collectorCount == 2) {
                    return mapAndCollect(mappings.get(0), mappings.get(1), collectors.get(0), collectors.get(1));
                } else {
                    throw new IllegalStateException(
                            "Invalid number of mappings (" + mappingCount + ") and collectors (" + collectorCount + ").");
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    private <A, GroupKey_> AbstractRuleBuilder map(Function<A, GroupKey_> groupKeyMapping) {
        BiFunction<PatternDef, Variable<GroupKey_>, PatternDef> binder =
                (pattern, tuple) -> pattern.bind(tuple, a -> groupKeyMapping.apply((A) a));
        return universalGroup(binder, (var, pattern, accumulate) -> regroup(var, pattern, accumulate));
    }

    private <InTuple> AbstractRuleBuilder universalGroup(BiFunction<PatternDef, Variable<InTuple>, PatternDef> bindFunction,
            Mutator<InTuple> mutator) {
        Variable<InTuple> mappedVariable = (Variable<InTuple>) declarationOf(Object.class, generateNextId("biMapped"));
        int patternId = getPrimaryPatterns().size() - 1;
        PatternDef mainAccumulatePattern = bindFunction.apply(getPrimaryPatterns().get(patternId), mappedVariable);
        List<ViewItem> dependentsExpressions = getDependentExpressionMap().getOrDefault(patternId, Collections.emptyList());
        ViewItem<?> innerAccumulatePattern = getInnerAccumulatePattern(mainAccumulatePattern, dependentsExpressions);
        Variable<Collection<InTuple>> tupleCollection =
                (Variable<Collection<InTuple>>) Util.createVariable(Collection.class, generateNextId("tupleCollection"));
        PatternDSL.PatternDef<Collection<InTuple>> pattern = pattern(tupleCollection)
                .expr("Non-empty", collection -> !collection.isEmpty(),
                        alphaIndexedBy(Integer.class, Index.ConstraintType.GREATER_THAN, -1, Collection::size, 0));
        ViewItem<Object> accumulate = DSL.accumulate(innerAccumulatePattern,
                accFunction(CollectSetAccumulateFunction.class, mappedVariable).as(tupleCollection));
        return mutator.apply(tupleCollection, pattern, accumulate);
    }

    private <A, GroupKeyA, GroupKeyB> AbstractRuleBuilder map(Function<A, GroupKeyA> groupKeyMappingA,
            Function<A, GroupKeyB> groupKeyMappingB) {
        return null;
    }

    private <A, Result> AbstractRuleBuilder collect(UniConstraintCollector<A, ?, Result> collector) {
        DroolsUniAccumulateFunction<A, ?, Result> bridge = new DroolsUniAccumulateFunction<>(collector);
        return collect(bridge);
    }

    private <A, GroupKeyA, ResultB> AbstractRuleBuilder mapAndCollect(Function<A, GroupKeyA> groupKeyMappingA,
            UniConstraintCollector<A, ?, ResultB> collectorB) {
        return groupWithCollect(() -> new DroolsUniToBiGroupByAccumulator<>(groupKeyMappingA, collectorB,
                getVariables().get(0)));
    }

    private <A, GroupKeyA, GroupKeyB, ResultC> AbstractRuleBuilder mapAndCollect(
            Function<A, GroupKeyA> groupKeyMappingA, Function<A, GroupKeyB> groupKeyMappingB,
            UniConstraintCollector<A, ?, ResultC> collectorC) {
        return null;
    }

    private <A, GroupKeyA, GroupKeyB, ResultC, ResultD> AbstractRuleBuilder mapAndCollect(
            Function<A, GroupKeyA> groupKeyMappingA, Function<A, GroupKeyB> groupKeyMappingB,
            UniConstraintCollector<A, ?, ResultC> collectorC, UniConstraintCollector<A, ?, ResultD> collectorD) {
        return null;
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

    @Override
    protected int getExpectedVariableCount() {
        return 1;
    }

    @Override
    protected <InTuple> PatternDef bindTupleVariableOnFirstGrouping(PatternDef pattern,
            Variable<InTuple> inTupleVariable) {
        return pattern.bind(inTupleVariable, fact -> fact);
    }
}
