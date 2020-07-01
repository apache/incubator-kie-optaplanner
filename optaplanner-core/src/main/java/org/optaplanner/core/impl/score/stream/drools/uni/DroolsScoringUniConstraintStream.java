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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.drools.model.Global;
import org.drools.model.RuleItemBuilder;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;

public final class DroolsScoringUniConstraintStream<Solution_, A> extends DroolsAbstractUniConstraintStream<Solution_, A> {

    private final DroolsAbstractUniConstraintStream<Solution_, A> parent;
    private final boolean noMatchWeigher;
    private final ToIntFunction<A> intMatchWeigher;
    private final ToLongFunction<A> longMatchWeigher;
    private final Function<A, BigDecimal> bigDecimalMatchWeigher;

    public DroolsScoringUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent) {
        this(constraintFactory, parent, true, null, null, null);
    }

    public DroolsScoringUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, ToIntFunction<A> intMatchWeigher) {
        this(constraintFactory, parent, false, intMatchWeigher, null, null);
        if (intMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public DroolsScoringUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, ToLongFunction<A> longMatchWeigher) {
        this(constraintFactory, parent, false, null, longMatchWeigher, null);
        if (longMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    public DroolsScoringUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, BigDecimal> bigDecimalMatchWeigher) {
        this(constraintFactory, parent, false, null, null, bigDecimalMatchWeigher);
        if (bigDecimalMatchWeigher == null) {
            throw new IllegalArgumentException("The matchWeigher (null) cannot be null.");
        }
    }

    private DroolsScoringUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, boolean noMatchWeigher,
            ToIntFunction<A> intMatchWeigher, ToLongFunction<A> longMatchWeigher,
            Function<A, BigDecimal> bigDecimalMatchWeigher) {
        super(constraintFactory);
        this.parent = parent;
        this.noMatchWeigher = noMatchWeigher;
        this.intMatchWeigher = intMatchWeigher;
        this.longMatchWeigher = longMatchWeigher;
        this.bigDecimalMatchWeigher = bigDecimalMatchWeigher;
    }

    @Override
    public List<DroolsFromUniConstraintStream<Solution_, Object>> getFromStreamList() {
        return parent.getFromStreamList();
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public List<RuleItemBuilder<?>> createRuleItemBuilders(DroolsConstraint<?> constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        DroolsUniCondition<A, ?> condition = parent.getCondition();
        if (intMatchWeigher != null) {
            return condition.completeWithScoring(constraint, scoreHolderGlobal, intMatchWeigher);
        } else if (longMatchWeigher != null) {
            return condition.completeWithScoring(constraint, scoreHolderGlobal, longMatchWeigher);
        } else if (bigDecimalMatchWeigher != null) {
            return condition.completeWithScoring(constraint, scoreHolderGlobal, bigDecimalMatchWeigher);
        } else if (noMatchWeigher) {
            return condition.completeWithScoring(scoreHolderGlobal);
        } else {
            throw new IllegalStateException("Impossible state: noMatchWeigher (" + noMatchWeigher + ").");
        }
    }

    @Override
    public DroolsUniCondition<A, ?> getCondition() {
        throw new UnsupportedOperationException("Scoring stream does not have its own UniCondition.");
    }

    @Override
    public Class[] getExpectedJustificationTypes() {
        return parent.getCondition().getExpectedJustificationTypes();
    }

    @Override
    public String toString() {
        return "Scoring()";
    }

}
