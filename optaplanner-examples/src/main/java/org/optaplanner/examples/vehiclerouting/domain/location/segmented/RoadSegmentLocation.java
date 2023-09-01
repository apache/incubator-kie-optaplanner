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

package org.optaplanner.examples.vehiclerouting.domain.location.segmented;

import java.util.Map;

import org.optaplanner.examples.common.persistence.jackson.KeySerializer;
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Like {@link RoadLocation},
 * but for high scale problems to avoid the memory issue of keeping the entire cost matrix in memory.
 * Used with {@link DistanceType#SEGMENTED_ROAD_DISTANCE}.
 */
public class RoadSegmentLocation extends Location {

    // Prefer Map over array or List because customers might be added and removed in real-time planning.
    protected Map<RoadSegmentLocation, Double> nearbyTravelDistanceMap;
    protected Map<HubSegmentLocation, Double> hubTravelDistanceMap;

    public RoadSegmentLocation() {
    }

    public RoadSegmentLocation(long id) {
        super(id);
    }

    public RoadSegmentLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    @JsonSerialize(keyUsing = KeySerializer.class)
    @JsonDeserialize(keyUsing = RoadSegmentLocationKeyDeserializer.class)
    public Map<RoadSegmentLocation, Double> getNearbyTravelDistanceMap() {
        return nearbyTravelDistanceMap;
    }

    public void setNearbyTravelDistanceMap(Map<RoadSegmentLocation, Double> nearbyTravelDistanceMap) {
        this.nearbyTravelDistanceMap = nearbyTravelDistanceMap;
    }

    @JsonSerialize(keyUsing = KeySerializer.class)
    @JsonDeserialize(keyUsing = HubSegmentLocationKeyDeserializer.class)
    public Map<HubSegmentLocation, Double> getHubTravelDistanceMap() {
        return hubTravelDistanceMap;
    }

    public void setHubTravelDistanceMap(Map<HubSegmentLocation, Double> hubTravelDistanceMap) {
        this.hubTravelDistanceMap = hubTravelDistanceMap;
    }

    @Override
    public long getDistanceTo(Location location) {
        Double distance = getDistanceDouble((RoadSegmentLocation) location);
        // Multiplied by 1000 to avoid floating point arithmetic rounding errors
        return (long) (distance * 1000.0 + 0.5);
    }

    public Double getDistanceDouble(RoadSegmentLocation location) {
        Double distance = nearbyTravelDistanceMap.get(location);
        if (distance == null) {
            // location isn't nearby
            distance = getShortestDistanceDoubleThroughHubs(location);
        }
        return distance;
    }

    protected double getShortestDistanceDoubleThroughHubs(RoadSegmentLocation location) {
        double shortestDistance = Double.MAX_VALUE;
        for (Map.Entry<HubSegmentLocation, Double> entry : hubTravelDistanceMap.entrySet()) {
            double distance = entry.getValue();
            distance += entry.getKey().getDistanceDouble(location);
            if (distance < shortestDistance) {
                shortestDistance = distance;
            }
        }
        return shortestDistance;
    }

}
