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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.drools.model.Global;
import org.drools.model.RuleItemBuilder;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.common.AbstractConstraintStream;
import org.optaplanner.core.impl.score.stream.common.ScoreImpactType;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsFromUniConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsScoringUniConstraintStream;

public abstract class DroolsAbstractConstraintStream<Solution_> extends AbstractConstraintStream<Solution_> {

    protected final DroolsConstraintFactory<Solution_> constraintFactory;
    private final List<DroolsAbstractConstraintStream<Solution_>> childStreamList = new ArrayList<>(2);

    public DroolsAbstractConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory) {
        this.constraintFactory = constraintFactory;
    }

    // ************************************************************************
    // Penalize/reward
    // ************************************************************************

    protected DroolsConstraint<Solution_> buildConstraint(String constraintPackage, String constraintName,
            Score<?> constraintWeight, ScoreImpactType impactType,
            DroolsAbstractConstraintStream<Solution_> scoringStream) {
        Function<Solution_, Score<?>> constraintWeightExtractor = buildConstraintWeightExtractor(
                constraintPackage, constraintName, constraintWeight);
        List<DroolsFromUniConstraintStream<Solution_, Object>> fromStreamList = getFromStreamList();
        return new DroolsConstraint<>(constraintFactory, constraintPackage, constraintName, constraintWeightExtractor,
                impactType, false, fromStreamList, scoringStream);
    }

    protected DroolsConstraint<Solution_> buildConstraintConfigurable(String constraintPackage, String constraintName,
            ScoreImpactType impactType, DroolsAbstractConstraintStream<Solution_> scoringStream) {
        Function<Solution_, Score<?>> constraintWeightExtractor = buildConstraintWeightExtractor(
                constraintPackage, constraintName);
        List<DroolsFromUniConstraintStream<Solution_, Object>> fromStreamList = getFromStreamList();
        return new DroolsConstraint<>(constraintFactory, constraintPackage, constraintName, constraintWeightExtractor,
                impactType, true, fromStreamList, scoringStream);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    public abstract List<DroolsFromUniConstraintStream<Solution_, Object>> getFromStreamList();

    public void addChildStream(DroolsAbstractConstraintStream<Solution_> childStream) {
        childStreamList.add(childStream);
    }

    public Collection<DroolsAbstractConstraintStream<Solution_>> getChildStreams() {
        return Collections.unmodifiableList(childStreamList);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    /**
     * Assemble elements of the rule that will process this stream and turn it into a constraint match. Will be ignored
     * unless on a scoring stream such as {@link DroolsScoringUniConstraintStream}.
     *
     * @param constraint constraint which is being scored
     * @param scoreHolderGlobal contains the score to be affected
     * @return rule representing this constraint stream
     */
    public List<RuleItemBuilder<?>> createRuleItemBuilders(DroolsConstraint<?> constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        throw new UnsupportedOperationException("Non-scoring stream (" + this + ") can not create a rule.");
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public DroolsConstraintFactory<Solution_> getConstraintFactory() {
        return constraintFactory;
    }

    /**
     * As defined by {@link DroolsRuleStructure#getExpectedJustificationTypes()}.
     * May only be called on scoring streams.
     *
     * @return never null, never empty
     */
    public Class[] getExpectedJustificationTypes() {
        throw new UnsupportedOperationException("Non-scoring stream (" + this + ") can not have any expected matches.");
    }

}
