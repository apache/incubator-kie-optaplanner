/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.examples.nurserostering.domain.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MinMaxContractLine extends ContractLine {

    private boolean minimumEnabled;
    private int minimumValue;
    private int minimumWeight;

    private boolean maximumEnabled;
    private int maximumValue;
    private int maximumWeight;

    public MinMaxContractLine() {
    }

    public MinMaxContractLine(long id, Contract contract, ContractLineType contractLineType, boolean minimumEnabled,
            boolean maximumEnabled) {
        super(id, contract, contractLineType);
        this.minimumEnabled = minimumEnabled;
        this.maximumEnabled = maximumEnabled;
    }

    public boolean isViolated(int count) {
        return getViolationAmount(count) != 0;
    }

    public int getViolationAmount(int count) {
        if (minimumEnabled && count < minimumValue) {
            return (minimumValue - count) * minimumWeight;
        } else if (maximumEnabled && count > maximumValue) {
            return (count - maximumValue) * maximumWeight;
        } else {
            return 0;
        }
    }

    public boolean isMinimumEnabled() {
        return minimumEnabled;
    }

    public void setMinimumEnabled(boolean minimumEnabled) {
        this.minimumEnabled = minimumEnabled;
    }

    public int getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
    }

    public int getMinimumWeight() {
        return minimumWeight;
    }

    public void setMinimumWeight(int minimumWeight) {
        this.minimumWeight = minimumWeight;
    }

    public boolean isMaximumEnabled() {
        return maximumEnabled;
    }

    public void setMaximumEnabled(boolean maximumEnabled) {
        this.maximumEnabled = maximumEnabled;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    public int getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(int maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return minimumEnabled || maximumEnabled;
    }

}
