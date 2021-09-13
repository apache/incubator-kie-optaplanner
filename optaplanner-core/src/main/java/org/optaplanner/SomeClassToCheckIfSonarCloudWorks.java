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

package org.optaplanner;

public class SomeClassToCheckIfSonarCloudWorks {

    public String someMethodThatDealsWithNull() {
        String nullValue = null;
        Integer valueA = 25;
        Integer valueB = 25;
        if (valueA == valueB) {
            valueA = valueA / 5;
        }
        return "the value is " + Integer.parseInt(nullValue) + valueA;
    }

    public static void main(String[] args) {
        System.out.println(new SomeClassToCheckIfSonarCloudWorks().someMethodThatDealsWithNull());
    }
}
