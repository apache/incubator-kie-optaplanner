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

package org.optaplanner.examples.nurserostering.domain;

import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import org.optaplanner.examples.common.swingui.components.Labeled;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Shift extends AbstractPersistable implements Labeled {

    private ShiftDate shiftDate;
    private ShiftType shiftType;
    private int index;

    private int requiredEmployeeSize;

    public Shift() {
    }

    public Shift(long id) {
        super(id);
    }

    public Shift(long id, ShiftDate shiftDate, ShiftType shiftType, int index, int requiredEmployeeSize) {
        this(id);
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.index = index;
        this.requiredEmployeeSize = requiredEmployeeSize;
    }

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRequiredEmployeeSize() {
        return requiredEmployeeSize;
    }

    public void setRequiredEmployeeSize(int requiredEmployeeSize) {
        this.requiredEmployeeSize = requiredEmployeeSize;
    }

    @Override
    public String getLabel() {
        return shiftType.getLabel() + " of " + shiftDate.getLabel();
    }

    @Override
    public String toString() {
        return shiftDate + "/" + shiftType;
    }

}
