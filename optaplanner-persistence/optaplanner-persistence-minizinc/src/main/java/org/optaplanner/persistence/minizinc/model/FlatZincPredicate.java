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

package org.optaplanner.persistence.minizinc.model;

import java.util.List;

public class FlatZincPredicate {
    private final String name;
    private final List<TypeNameValue> argList;

    public FlatZincPredicate(String name, List<TypeNameValue> argList) {
        this.name = name;
        this.argList = argList;
    }

    public String getName() {
        return name;
    }

    public List<TypeNameValue> getArgList() {
        return argList;
    }

    @Override
    public String toString() {
        return "FlatZincPredicate{" +
                "name='" + name + '\'' +
                ", argList=" + argList +
                '}';
    }
}
