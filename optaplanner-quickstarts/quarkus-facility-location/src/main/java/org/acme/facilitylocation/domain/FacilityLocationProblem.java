/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.facilitylocation.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class FacilityLocationProblem {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "facilityRange")
    private List<Facility> facilities;
    @PlanningEntityCollectionProperty
    private List<DemandPoint> demandPoints;

    @PlanningScore
    private HardSoftScore score;

    public FacilityLocationProblem() {
    }

    public FacilityLocationProblem(List<Facility> facilities, List<DemandPoint> demandPoints, HardSoftScore score) {
        this.facilities = facilities;
        this.demandPoints = demandPoints;
        this.score = score;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<Facility> facilities) {
        this.facilities = facilities;
    }

    public List<DemandPoint> getDemandPoints() {
        return demandPoints;
    }

    public void setDemandPoints(List<DemandPoint> demandPoints) {
        this.demandPoints = demandPoints;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "FacilityLocationProblem{" +
                "facilities: " + facilities.size() +
                ", demandPoints: " + demandPoints.size() +
                ", score: " + score +
                '}';
    }
}
