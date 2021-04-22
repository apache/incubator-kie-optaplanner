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

package org.optaplanner.quarkus.devui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationProperty {
    String name;
    String value;
    ValueType valueType;
    List<String> enumValueList;

    public ConfigurationProperty(String name, String value, ValueType valueType) {
        this(name, value, valueType, null);
    }

    public ConfigurationProperty(String name, String value, Class<? extends Enum> enumClass) {
        this(name, value, ValueType.ENUM,
                Arrays.stream(enumClass.getEnumConstants())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
    }

    public ConfigurationProperty(String name, String value, ValueType valueType, List<String> enumValueList) {
        this.name = name;
        this.value = value;
        this.valueType = valueType;

        if (this.valueType == ValueType.ENUM && enumValueList == null) {
            throw new IllegalArgumentException("enumValueList must be set for valueType (" + valueType + ").");
        } else if (this.valueType != ValueType.ENUM && enumValueList != null) {
            throw new IllegalArgumentException("enumValueList must be null for valueType (" + valueType + ").");
        }

        this.enumValueList = enumValueList;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public List<String> getEnumValueList() {
        return enumValueList;
    }

    public enum ValueType {
        ENUM,
        NUMBER,
        STRING;
    }
}
