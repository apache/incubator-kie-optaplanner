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

package org.optaplanner.persistence.minizinc.backend.gizmo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledVariablesAndConstantsData {
    private final Map<String, List<Class<?>>> variableToMarkerClassListMap;
    private final Map<String, Map<Class<?>, List<Integer>>> variableToMarkerClassToListIndexesMap;
    private final Map<Object, List<Class<?>>> constantToMarkerClassListMap;
    private final Map<Object, Map<Class<?>, List<Integer>>> constantToMarkerClassToListIndexesMap;

    public CompiledVariablesAndConstantsData() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public CompiledVariablesAndConstantsData(Map<String, List<Class<?>>> variableToMarkerClassListMap,
            Map<String, Map<Class<?>, List<Integer>>> variableToMarkerClassToListIndexesMap,
            Map<Object, List<Class<?>>> constantToMarkerClassListMap,
            Map<Object, Map<Class<?>, List<Integer>>> constantToMarkerClassToListIndexesMap) {
        this.variableToMarkerClassListMap = variableToMarkerClassListMap;
        this.variableToMarkerClassToListIndexesMap = variableToMarkerClassToListIndexesMap;
        this.constantToMarkerClassListMap = constantToMarkerClassListMap;
        this.constantToMarkerClassToListIndexesMap = constantToMarkerClassToListIndexesMap;
    }

    public Map<String, List<Class<?>>> getVariableToMarkerClassListMap() {
        return variableToMarkerClassListMap;
    }

    public Map<String, Map<Class<?>, List<Integer>>> getVariableToMarkerClassToListIndexesMap() {
        return variableToMarkerClassToListIndexesMap;
    }

    public Map<Object, List<Class<?>>> getConstantToMarkerClassListMap() {
        return constantToMarkerClassListMap;
    }

    public Map<Object, Map<Class<?>, List<Integer>>> getConstantToMarkerClassToListIndexesMap() {
        return constantToMarkerClassToListIndexesMap;
    }
}
