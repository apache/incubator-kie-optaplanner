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

import org.drools.model.*;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate2;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.score.stream.bi.AbstractBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

import java.util.Collections;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.betaIndexedBy;

class PatternVariable<A> {

    private final Variable<A> primaryVariable;
    // Patterns in Drools are mutable, yet we need want to share them. Therefore we need to create them on-demand.
    private final Supplier<PatternDSL.PatternDef<A>> patternSupplier;
    // Expressions that are required before the current pattern in order to be able to reach it, such as groupBy().
    private final List<ViewItem<?>> prerequisiteExpressions;
    // Expressions that follow up this pattern without influencing others, such as exists().
    private final List<ViewItem<?>> dependentExpressions;

    PatternVariable(Variable<A> aVariable) {
        this(aVariable, Collections.emptyList());
    }

    PatternVariable(Variable<A> aVariable, List<ViewItem<?>> prerequisiteExpressions) {
        this.primaryVariable = aVariable;
        this.patternSupplier = () -> PatternDSL.pattern(aVariable);
        this.prerequisiteExpressions = prerequisiteExpressions;
        this.dependentExpressions = Collections.emptyList();
    }

    PatternVariable(PatternVariable<A> patternCreator, UnaryOperator<PatternDSL.PatternDef<A>> patternMutator) {
        this.primaryVariable = patternCreator.primaryVariable;
        this.patternSupplier = () -> patternMutator.apply(patternCreator.patternSupplier.get());
        this.prerequisiteExpressions = patternCreator.prerequisiteExpressions;
        this.dependentExpressions = patternCreator.dependentExpressions;
    }

    PatternVariable(PatternVariable<A> patternCreator, ViewItem<?> dependentExpression) {
        this.primaryVariable = patternCreator.primaryVariable;
        this.patternSupplier = patternCreator.patternSupplier;
        this.prerequisiteExpressions = patternCreator.prerequisiteExpressions;
        this.dependentExpressions = Stream.concat(patternCreator.dependentExpressions.stream(), Stream.of(dependentExpression))
                .collect(Collectors.toList());

    }

    public Variable<A> getPrimaryVariable() {
        return primaryVariable;
    }

    public PatternVariable<A> filter(Predicate<A> predicate) {
        return new PatternVariable<>(this, p -> {
            AlphaIndex<A, Boolean> index =
                    alphaIndexedBy(Boolean.class, Index.ConstraintType.EQUAL, 0, predicate::test, Boolean.TRUE);
            return p.expr("Filter using " + predicate, predicate::test, index);
        });
    }

    public <LeftJoinVar_> PatternVariable<A> filter(BiPredicate<LeftJoinVar_, A> predicate,
            Variable<LeftJoinVar_> leftJoinVariable) {
        return new PatternVariable<>(this,
                p -> p.expr("Filter using " + predicate, leftJoinVariable, (a, leftJoinVar) -> predicate.test(leftJoinVar, a)));
    }

    public <LeftJoinVarA_, LeftJoinVarB_> PatternVariable<A> filter(
            TriPredicate<LeftJoinVarA_, LeftJoinVarB_, A> predicate, Variable<LeftJoinVarA_> leftJoinVariableA,
            Variable<LeftJoinVarB_> leftJoinVariableB) {
        return new PatternVariable<>(this, p -> p.expr("Filter using " + predicate, leftJoinVariableA, leftJoinVariableB,
                (a, leftJoinVarA, leftJoinVarB) -> predicate.test(leftJoinVarA, leftJoinVarB, a)));
    }

    public <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> PatternVariable<A> filter(
            QuadPredicate<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, A> predicate,
            Variable<LeftJoinVarA_> leftJoinVariableA, Variable<LeftJoinVarB_> leftJoinVariableB,
            Variable<LeftJoinVarC_> leftJoinVariableC) {
        return new PatternVariable<>(this,
                p -> p.expr("Filter using " + predicate, leftJoinVariableA, leftJoinVariableB, leftJoinVariableC,
                        (a, leftJoinVarA, leftJoinVarB, leftJoinVarC) -> predicate.test(leftJoinVarA, leftJoinVarB,
                                leftJoinVarC, a)));
    }

    public <LeftJoinVar_> PatternVariable<A> filterOnJoinVar(Variable<LeftJoinVar_> leftJoinVar,
            AbstractBiJoiner<LeftJoinVar_, A> joiner, JoinerType joinerType, int mappingIndex) {
        // For each mapping, bind a join variable from A to B and index the binding.
        Function<LeftJoinVar_, Object> leftMapping = joiner.getLeftMapping(mappingIndex);
        Function<A, Object> rightMapping = joiner.getRightMapping(mappingIndex);
        Function1<A, Object> rightExtractor = rightMapping::apply;
        // Only extract B; A is coming from a pre-bound join var.
        Predicate2<A, LeftJoinVar_> predicate = (b, a) -> joinerType.matches(a, rightExtractor.apply(b));
        return new PatternVariable<>(this, p -> {
            BetaIndex<A, LeftJoinVar_, Object> index = betaIndexedBy(Object.class, Mutator.getConstraintType(joinerType),
                    mappingIndex, rightExtractor, leftMapping::apply);
            return p.expr("Join using joiner #" + mappingIndex + " in " + joiner, leftJoinVar, predicate, index);
        });
    }

    public <BoundVar_> PatternVariable<A> bind(Variable<BoundVar_> boundVariable, Function<A, BoundVar_> bindingFunction) {
        return new PatternVariable<>(this, p -> p.bind(boundVariable, bindingFunction::apply));
    }

    public <BoundVar_, LeftJoinVar_> PatternVariable<A> bind(Variable<BoundVar_> boundVariable,
            Variable<LeftJoinVar_> leftJoinVariable, BiFunction<A, LeftJoinVar_, BoundVar_> bindingFunction) {
        return new PatternVariable<>(this, p -> p.bind(boundVariable, leftJoinVariable, bindingFunction::apply));
    }

    public <BoundVar_, LeftJoinVarA_, LeftJoinVarB_> PatternVariable<A> bind(Variable<BoundVar_> boundVariable,
            Variable<LeftJoinVarA_> leftJoinVariableA, Variable<LeftJoinVarB_> leftJoinVariableB,
            TriFunction<A, LeftJoinVarA_, LeftJoinVarB_, BoundVar_> bindingFunction) {
        return new PatternVariable<>(this,
                p -> p.bind(boundVariable, leftJoinVariableA, leftJoinVariableB, bindingFunction::apply));
    }

    public <BoundVar_, LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> PatternVariable<A> bind(
            Variable<BoundVar_> boundVariable, Variable<LeftJoinVarA_> leftJoinVariableA,
            Variable<LeftJoinVarB_> leftJoinVariableB, Variable<LeftJoinVarC_> leftJoinVariableC,
            QuadFunction<A, LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, BoundVar_> bindingFunction) {
        return new PatternVariable<>(this,
                p -> p.bind(boundVariable, leftJoinVariableA, leftJoinVariableB, leftJoinVariableC,
                        bindingFunction::apply));
    }

    public PatternVariable<A> addDependentExpression(ViewItem<?> expression) {
        return new PatternVariable<>(this, expression);
    }

    public List<ViewItem<?>> build() {
        Stream<ViewItem<?>> prerequisites = prerequisiteExpressions.stream();
        Stream<ViewItem<?>> dependents = dependentExpressions.stream();
        return Stream.concat(Stream.concat(prerequisites, Stream.of(patternSupplier.get())), dependents)
                .collect(Collectors.toList());
    }

}
