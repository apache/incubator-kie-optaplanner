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

import java.util.Collections;
import java.util.Map;

import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;

public final class SingleConstraintVerifierAssertion<Solution_>
        extends AbstractConstraintVerifierAssertion<SingleConstraintVerifierAssertion<Solution_>,
        SingleConstraintVerifier<Solution_>> {

    private final Map<String, ConstraintMatchTotal> constraintMatchTotalMap;

    SingleConstraintVerifierAssertion(SingleConstraintVerifier<Solution_> singleConstraintVerifier,
            Map<String, ConstraintMatchTotal> constraintMatchTotalMap) {
        super(singleConstraintVerifier);
        this.constraintMatchTotalMap = Collections.unmodifiableMap(constraintMatchTotalMap);
    }

    @Override
    protected Number getImpact() {
        return constraintMatchTotalMap.values().stream()
                .mapToInt(ConstraintMatchTotal::getConstraintMatchCount)
                .sum();
    }
}
