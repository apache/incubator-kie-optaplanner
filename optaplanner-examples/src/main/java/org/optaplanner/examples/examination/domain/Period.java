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

package org.optaplanner.examples.examination.domain;

import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import org.optaplanner.examples.common.swingui.components.Labeled;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Period extends AbstractPersistable implements Labeled {

    private String startDateTimeString;
    private int periodIndex;
    private int dayIndex;
    private int duration; // in minutes
    private int penalty;
    private boolean frontLoadLast;

    public String getStartDateTimeString() {
        return startDateTimeString;
    }

    public void setStartDateTimeString(String startDateTimeString) {
        this.startDateTimeString = startDateTimeString;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public boolean isFrontLoadLast() {
        return frontLoadLast;
    }

    public void setFrontLoadLast(boolean frontLoadLast) {
        this.frontLoadLast = frontLoadLast;
    }

    @Override
    public String getLabel() {
        return startDateTimeString;
    }

    @Override
    public String toString() {
        return startDateTimeString;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Period withId(long id) {
        this.setId(id);
        return this;
    }

    public Period withStartDateTimeString(String startDateTimeString) {
        this.setStartDateTimeString(startDateTimeString);
        return this;
    }

    public Period withPeriodIndex(int periodIndex) {
        this.setPeriodIndex(periodIndex);
        return this;
    }

    public Period withDayIndex(int dayIndex) {
        this.setDayIndex(dayIndex);
        return this;
    }

    public Period withDuration(int duration) {
        this.setDuration(duration);
        return this;
    }

    public Period withPenalty(int penalty) {
        this.setPenalty(penalty);
        return this;
    }

    public Period withFrontLoadLast(boolean frontLoadLast) {
        this.setFrontLoadLast(frontLoadLast);
        return this;
    }
}
