/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.examples.vehiclerouting.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@XStreamAlias("VrpLocation")
public class Location extends AbstractPersistable {

    private String name = null;
    private double latitude;
    private double longitude;

    @XStreamOmitField
    protected CostMatrix costMatrix;

    @XStreamOmitField
    protected int index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * The distance is not in miles or km, but in the TSPLIB's unit of measurement.
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public int getDistance(Location location) {
        final int res;
        if (costMatrix != null) {
            res = costMatrix.get(location.getIndex(), index);
        } else {
            // Implementation specified by TSPLIB http://www2.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/
            // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
            double latitudeDifference = location.latitude - latitude;
            double longitudeDifference = location.longitude - longitude;
            double distance = Math.sqrt(
                    (latitudeDifference * latitudeDifference) + (longitudeDifference * longitudeDifference));
            res = (int) (distance * 1000.0 + 0.5);
        }
        return res;
    }

    @Override
    public String toString() {
        if (name == null) {
            return id.toString();
        }
        return id.toString() + "-" + name;
    }

    public String getSafeName() {
        if (name == null) {
            return id.toString();
        }
        return name;
    }

    public void setCostMatrix(CostMatrix costMatrix) {
        this.costMatrix = costMatrix;
    }

    public CostMatrix getCostMatrix() {
        return costMatrix;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
