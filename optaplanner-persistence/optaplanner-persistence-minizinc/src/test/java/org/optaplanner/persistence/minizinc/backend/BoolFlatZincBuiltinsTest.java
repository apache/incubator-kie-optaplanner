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

package org.optaplanner.persistence.minizinc.backend;

import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class BoolFlatZincBuiltinsTest {

    final ConstraintVerifier<BoolFlatZincBuiltinsTestConstraintProvider, MyPlanningSolution> constraintVerifier =
            ConstraintVerifier
                    .build(new BoolFlatZincBuiltinsTestConstraintProvider(), MyPlanningSolution.class,
                            MyBoolVariable.class,
                            MyBoolArrayVariable.class);

    @Test
    public void test_array_bool_and() {
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(0, true),
                        new MyBoolVariable(true))
                .penalizes(1);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, false),
                        new MyBoolArrayVariable(0, true),
                        new MyBoolVariable(false))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(0, true),
                        new MyBoolVariable(true))
                .penalizes(0);
        constraintVerifier.verifyThat(BoolFlatZincBuiltinsTestConstraintProvider::array_bool_and)
                .given(new MyBoolArrayVariable(0, true),
                        new MyBoolArrayVariable(0, true),
                        new MyBoolVariable(false))
                .penalizes(1);
    }
}
