/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.constraint.streams.drools.common;

import java.util.function.Function;

import org.drools.model.Variable;
import org.drools.model.view.ViewItem;

/**
 * Represents a single variable with all of its patterns in the left hand side of a Drools rule,
 * which doesn't use the pattern's variable but instead binds another on that pattern.
 *
 * <p>
 * Consider the following simple bivariate rule, in the equivalent DRL:
 *
 * <pre>
 * {@code
 *  rule "Simple bivariate rule"
 *  when
 *      $a: Something()
 *      SomethingElse($b: someField)
 *  then
 *      // Do something with the $a and $b variables.
 *  end
 * }
 * </pre>
 * <p>
 * In this rule, variable "a" would be represented by {@link DirectPatternVariable}.
 * Variable "b" would be represented by this class and would be extracted from SomethingElse using a mapping function.
 *
 * <p>
 * Therefore although all the operations of {@link PatternVariable} have the same semantics here and in
 * {@link DirectPatternVariable}, indirect variables need to apply a level of indirection to get their values from the
 * pattern.
 * This will require repeated invocations of the mapping function (see {@link #extract(Object)},
 * which must therefore be efficiently implemented, ideally a pure stateless getter.
 *
 * <p>
 * These repeated invocations are a Drools performance trade-off. If we instead bound the variable on the pattern,
 * we would subsequently have to increase the arity of all binding/expression executable model functions by that
 * one bound variable.
 * Unfortunately, this would have been inefficient, as that would prevent these higher-arity functions from being
 * properly JITted and the performance would arguably suffer more than when we have to call an inexpensive mapping
 * function which would likely be optimized by the JIT anyway.
 *
 * @param <A> generic type of the primary variable as obtained by the mapping function from the pattern variable
 * @param <PatternVar_>> generic type of the pattern variable
 */
final class IndirectPatternVariable<A, PatternVar_>
        extends AbstractPatternVariable<A, PatternVar_, IndirectPatternVariable<A, PatternVar_>> {

    private final Function<PatternVar_, A> mappingFunction;

    <OldA> IndirectPatternVariable(IndirectPatternVariable<OldA, PatternVar_> patternCreator, Variable<A> boundVariable,
            Function<OldA, A> mappingFunction) {
        super(patternCreator, boundVariable);
        this.mappingFunction = patternCreator.mappingFunction.andThen(mappingFunction);
    }

    IndirectPatternVariable(DirectPatternVariable<PatternVar_> patternCreator, Variable<A> boundVariable,
            Function<PatternVar_, A> mappingFunction) {
        super(patternCreator, boundVariable);
        this.mappingFunction = mappingFunction;
    }

    private IndirectPatternVariable(IndirectPatternVariable<A, PatternVar_> patternCreator,
            ViewItem<?> dependentExpression) {
        super(patternCreator, dependentExpression);
        this.mappingFunction = patternCreator.mappingFunction;
    }

    @Override
    protected A extract(PatternVar_ patternVar) {
        // Value of an indirect variable is a result of applying a mapping on the pattern variable.
        return mappingFunction.apply(patternVar);
    }

    @Override
    public IndirectPatternVariable<A, PatternVar_> addDependentExpression(ViewItem<?> expression) {
        return new IndirectPatternVariable<>(this, expression);
    }

}
