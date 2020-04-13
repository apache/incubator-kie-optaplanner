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

package org.optaplanner.test.api.score.stream;

import java.util.Map;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.stream.ConstraintStreamScoreDirectorFactory;

public final class MultiConstraintVerification<Solution_>
        extends AbstractVerification<Solution_, MultiConstraintAssertion<Solution_>> {

    private final ConstraintProvider constraintProvider;

    protected MultiConstraintVerification(SolutionDescriptor<Solution_> solutionDescriptor,
            ConstraintProvider constraintProvider, ConstraintStreamImplType constraintStreamImplType) {
        super(new ConstraintStreamScoreDirectorFactory<>(solutionDescriptor, constraintProvider,
                constraintStreamImplType));
        this.constraintProvider = constraintProvider;
    }

    protected ConstraintProvider getConstraintProvider() {
        return constraintProvider;
    }

    @Override
    protected MultiConstraintAssertion<Solution_> createAssertion(Score<?> score,
            Map<String, ConstraintMatchTotal> constraintMatchTotalMap) {
        return new MultiConstraintAssertion<>(this, score);
    }
    
}
