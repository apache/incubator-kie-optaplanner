/*
 * Copyright 2015 JBoss Inc
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

package org.optaplanner.examples.vehiclerouting.domain.timewindowed.solver;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;

// TODO When this class is added only for TimeWindowedCustomer, use TimeWindowedCustomer instead of Customer
public class ArrivalTimeUpdatingVariableListener implements VariableListener<Customer> {

    public void beforeEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateArrivalTime(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateArrivalTime(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    protected void updateArrivalTime(ScoreDirector scoreDirector, TimeWindowedCustomer sourceCustomer) {
        Standstill previousStandstill = sourceCustomer.getPreviousStandstill();
        Long departureTime = (previousStandstill instanceof TimeWindowedCustomer)
                ? ((TimeWindowedCustomer) previousStandstill).getDepartureTime() : null;

        int arrivalDay = (previousStandstill instanceof TimeWindowedCustomer)
                ? ((TimeWindowedCustomer) previousStandstill).getarrivalDay() : 0;

        TimeWindowedCustomer shadowCustomer = sourceCustomer;

        Long arrivalTime = calculateArrivalTime(shadowCustomer, departureTime, arrivalDay);
        while (shadowCustomer != null && ObjectUtils.notEqual(shadowCustomer.getArrivalTime(), arrivalTime)) {
            scoreDirector.beforeVariableChanged(shadowCustomer, "arrivalTime");
            shadowCustomer.setArrivalTime(arrivalTime);
            scoreDirector.afterVariableChanged(shadowCustomer, "arrivalTime");
            departureTime = shadowCustomer.getDepartureTime();
            arrivalDay = shadowCustomer.getarrivalDay();
            shadowCustomer = shadowCustomer.getNextCustomer();
            arrivalTime = calculateArrivalTime(shadowCustomer, departureTime, arrivalDay);
        }
    }

    private long dayS(int i){
    	return i * 24 * 60 * 1000;
    }

    private Long calculateArrivalTime(TimeWindowedCustomer customer, Long previousDepartureTime, int arrivalDay) {

    	if (customer == null) {
    		return null;
    	}

    	long arrivalTime;

    	if (previousDepartureTime == null) {
    		// PreviousStandstill is the Vehicle, so we leave from the Depot at
    		// the best suitable time

    		customer.setarrivalDay(customer.getdeliveryRangeStart());
    		long a = dayS(customer.getdeliveryRangeStart()) + customer.getReadyTime();
    		long b = customer.getDistanceFromPreviousStandstill();
    		arrivalTime = Math.max(a, b);
    	} else {
    		arrivalTime = previousDepartureTime + customer.getDistanceFromPreviousStandstill();
    	}

    	if (arrivalDay < customer.getdeliveryRangeStart()) {
    		//If arrival is before the delivery range then the arrival day is set to the
    		//first day of the delivery range.

    		arrivalDay = customer.getdeliveryRangeStart();
    		customer.setarrivalDay(customer.getdeliveryRangeStart());
    		arrivalTime = dayS(arrivalDay) + customer.getReadyTime();
    		return arrivalTime;
    	}

    	if (arrivalTime > dayS(arrivalDay) + customer.getDueTime()) {
    		//If arrival is after the dueTime, for the current day of arrival
    		//the time of arrival is set to the readyTime of the next day.

    		customer.setarrivalDay(arrivalDay + 1);
    		arrivalTime = dayS(arrivalDay + 1) + customer.getReadyTime();
    		return arrivalTime;
    	} else {
    		customer.setarrivalDay(arrivalDay);
    		return arrivalTime;

    	}
    }

}
