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

import java.util.List;

/**
 * Contains builtin predicates for the FlatZinc language.
 * Note: is_defined_var / defines_var pair is implemented
 * using CustomShadowVariable (See https://www.minizinc.org/doc-2.5.5/en/lib-stdlib.html#index-13 /
 * https://www.minizinc.org/doc-2.5.5/en/lib-stdlib.html#index-23).
 * See https://www.minizinc.org/doc-2.5.5/en/lib-flatzinc.html
 */
public class FlatZincBuiltins {
    // Private default constructor since this is a factory class
    private FlatZincBuiltins() {
    }

    public static List<Class<?>> getConstraintFactoryList() {
        return List.of(IntFlatZincBuiltins.class);
    }
}
