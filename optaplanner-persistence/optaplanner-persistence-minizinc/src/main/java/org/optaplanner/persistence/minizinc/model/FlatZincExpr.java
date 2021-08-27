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

import java.math.BigDecimal;

public class FlatZincExpr {
    Object value;

    FlatZincExpr() {
        this.value = null;
    }

    public FlatZincExpr(boolean value) {
        this.value = value;
    }

    public FlatZincExpr(int value) {
        this.value = value;
    }

    public FlatZincExpr(BigDecimal value) {
        this.value = value;
    }

    public FlatZincExpr(int[] value) {
        this.value = value;
    }

    public FlatZincExpr(Integer[] value) {
        int[] intArray = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            intArray[i] = value[i];
        }
        this.value = intArray;
    }

    public FlatZincExpr(BigDecimal[] value) {
        this.value = value;
    }

    public FlatZincExpr(String identifier) {
        this.value = identifier;
    }

    public boolean isLiteral() {
        return !isVariable();
    }

    public boolean isVariable() {
        return String.class.equals(value.getClass());
    }

    public boolean asBoolean() {
        return (boolean) value;
    }

    public int asInt() {
        return (int) value;
    }

    public BigDecimal asFloat() {
        return (BigDecimal) value;
    }

    public int[] asIntSet() {
        return (int[]) value;
    }

    public BigDecimal[] asFloatSet() {
        return (BigDecimal[]) value;
    }

    public String asVariable() {
        return (String) value;
    }

    public FlatZincAnnotation asAnnotation() {
        return (FlatZincAnnotation) this;
    }

    @Override
    public String toString() {
        return "FlatZincExpr{" +
                "value=" + value +
                '}';
    }
}
