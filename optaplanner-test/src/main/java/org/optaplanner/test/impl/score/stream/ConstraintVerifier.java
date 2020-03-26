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

package org.optaplanner.test.impl.score.stream;

import java.util.function.Function;
import java.util.stream.Stream;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

public final class ConstraintVerifier<Solution_> {

    public static <Solution_> ConstraintVerifier<Solution_> createFor(Class<Solution_> planningSolutionClass,
            Class<?> firstPlanningEntityClass, Class<?>... otherPlanningEntityClasses) {
        Class[] entityClasses = Stream.concat(Stream.of(firstPlanningEntityClass), Stream.of(otherPlanningEntityClasses))
                .toArray(Class[]::new);
        SolutionDescriptor<Solution_> solutionDescriptor =
                SolutionDescriptor.buildSolutionDescriptor(planningSolutionClass, entityClasses);
        return new ConstraintVerifier<>(solutionDescriptor);
    }

    private final SolutionDescriptor<Solution_> solutionDescriptor;

    private ConstraintVerifier(SolutionDescriptor<Solution_> solutionDescriptor) {
        this.solutionDescriptor = solutionDescriptor;
    }

    SolutionDescriptor<Solution_> getSolutionDescriptor() {
        return solutionDescriptor;
    }

    public SingleConstraintVerifier<Solution_> forConstraint(Function<ConstraintFactory, Constraint> constraintFunction) {
        return forConstraint(constraintFunction, ConstraintStreamImplType.DROOLS);
    }

    public SingleConstraintVerifier<Solution_> forConstraint(Function<ConstraintFactory, Constraint> constraintFunction,
            ConstraintStreamImplType constraintStreamImplType) {
        return new SingleConstraintVerifier<>(this, constraintFunction, constraintStreamImplType);
    }

}
