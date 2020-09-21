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

package org.optaplanner.examples.tennis.optional.score;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.impl.score.stream.bi.DefaultBiConstraintCollector;
import org.optaplanner.core.impl.score.stream.uni.DefaultUniConstraintCollector;
import org.optaplanner.examples.common.solver.drools.functions.LoadBalanceByCountAccumulateFunction;
import org.optaplanner.examples.common.solver.drools.functions.LoadBalanceByCountAccumulateFunction.LoadBalanceByCountData;
import org.optaplanner.examples.common.solver.drools.functions.LoadBalanceByCountAccumulateFunction.LoadBalanceByCountResult;
import org.optaplanner.examples.tennis.domain.TeamAssignment;
import org.optaplanner.examples.tennis.domain.UnavailabilityPenalty;

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

public final class TennisConstraintProvider implements ConstraintProvider {

    private static final String CONSTRAINT_PACKAGE = "org.optaplanner.examples.tennis.solver";

    private static <A> DefaultUniConstraintCollector<A, ?, LoadBalanceByCountResult>
            loadBalanceByCount(Function<A, Object> groupKey) {
        LoadBalanceByCountAccumulateFunction accumulateFunction = new LoadBalanceByCountAccumulateFunction();
        return new DefaultUniConstraintCollector<>(
                () -> {
                    LoadBalanceByCountData resultContainer = accumulateFunction.createContext();
                    accumulateFunction.init(resultContainer);
                    return resultContainer;
                },
                (resultContainer, a) -> {
                    Object mapped = groupKey.apply(a);
                    accumulateFunction.accumulate(resultContainer, mapped);
                    return () -> accumulateFunction.reverse(resultContainer, mapped);
                },
                accumulateFunction::getResult);
    }

    private static <A, B> DefaultBiConstraintCollector<A, B, ?, LoadBalanceByCountResult>
            loadBalanceByCount(BiFunction<A, B, Object> groupKey) {
        LoadBalanceByCountAccumulateFunction accumulateFunction = new LoadBalanceByCountAccumulateFunction();
        return new DefaultBiConstraintCollector<>(
                () -> {
                    LoadBalanceByCountData resultContainer = accumulateFunction.createContext();
                    accumulateFunction.init(resultContainer);
                    return resultContainer;
                },
                (resultContainer, a, b) -> {
                    Object mapped = groupKey.apply(a, b);
                    accumulateFunction.accumulate(resultContainer, mapped);
                    return () -> accumulateFunction.reverse(resultContainer, mapped);
                },
                accumulateFunction::getResult);
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                oneAssignmentPerDatePerTeam(constraintFactory),
                unavailabilityPenalty(constraintFactory),
                fairAssignmentCountPerTeam(constraintFactory),
                evenlyConfrontationCount(constraintFactory)
        };
    }

    protected Constraint oneAssignmentPerDatePerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getTeam),
                        equal(TeamAssignment::getDay),
                        lessThan(TeamAssignment::getId))
                .penalize(CONSTRAINT_PACKAGE, "oneAssignmentPerDatePerTeam", HardMediumSoftScore.ONE_HARD);
    }

    protected Constraint unavailabilityPenalty(ConstraintFactory constraintFactory) {
        return constraintFactory.from(UnavailabilityPenalty.class)
                .ifExists(TeamAssignment.class,
                        equal(UnavailabilityPenalty::getTeam, TeamAssignment::getTeam),
                        equal(UnavailabilityPenalty::getDay, TeamAssignment::getDay))
                .penalize(CONSTRAINT_PACKAGE, "unavailabilityPenalty", HardMediumSoftScore.ONE_HARD);
    }

    protected Constraint fairAssignmentCountPerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TeamAssignment.class)
                .groupBy(loadBalanceByCount(TeamAssignment::getTeam))
                .penalize(CONSTRAINT_PACKAGE, "fairAssignmentCountPerTeam", HardMediumSoftScore.ONE_MEDIUM,
                        (result) -> (int) result.getZeroDeviationSquaredSumRootMillis());
    }

    protected Constraint evenlyConfrontationCount(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TeamAssignment.class)
                .join(TeamAssignment.class,
                        equal(TeamAssignment::getDay),
                        lessThan(assignment -> assignment.getTeam().getId()))
                .groupBy(loadBalanceByCount(
                        (assignment, otherAssignment) -> Pair.of(assignment.getTeam(), otherAssignment.getTeam())))
                .penalize(CONSTRAINT_PACKAGE, "evenlyConfrontationCount", HardMediumSoftScore.ONE_SOFT,
                        result -> (int) result.getZeroDeviationSquaredSumRootMillis());
    }

}
