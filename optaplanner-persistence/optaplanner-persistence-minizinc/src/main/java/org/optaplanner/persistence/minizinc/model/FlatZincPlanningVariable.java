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
import java.util.Objects;
import java.util.Optional;

import org.optaplanner.core.api.domain.valuerange.ValueRange;

public class FlatZincPlanningVariable {
    private final ValueRange valueRange;
    private final String name;
    private final List<FlatZincAnnotation> annotationList;
    private final Optional<FlatZincExpr> defaultValue;

    public FlatZincPlanningVariable(ValueRange valueRange, String name, List<FlatZincAnnotation> annotationList,
            FlatZincExpr defaultValue) {
        this.name = name;
        this.valueRange = valueRange;
        this.annotationList = annotationList;
        this.defaultValue = Optional.of(defaultValue);
    }

    public FlatZincPlanningVariable(ValueRange valueRange, String name, List<FlatZincAnnotation> annotationList) {
        this.name = name;
        this.valueRange = valueRange;
        this.annotationList = annotationList;
        this.defaultValue = Optional.empty();
    }

    public String getName() {
        return name;
    }

    public ValueRange getValueRange() {
        return valueRange;
    }

    public List<FlatZincAnnotation> getAnnotationList() {
        return annotationList;
    }

    public Optional<FlatZincExpr> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "FlatZincPlanningVariable{" +
                "valueRange=" + valueRange +
                ", name='" + name + '\'' +
                ", annotationList=" + annotationList +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlatZincPlanningVariable that = (FlatZincPlanningVariable) o;
        return valueRange.equals(that.valueRange) && name.equals(that.name) && annotationList.equals(that.annotationList)
                && defaultValue.equals(that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueRange, name, annotationList, defaultValue);
    }
}
