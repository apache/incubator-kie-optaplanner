/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.examples.nqueens.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.PlanningVariable;
import org.drools.planner.api.domain.variable.ValueRangeFromSolutionProperty;
import org.drools.planner.examples.common.domain.AbstractPersistable;

@PlanningEntity
public class Queen extends AbstractPersistable implements Comparable<Queen> {

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @PlanningVariable
    @ValueRangeFromSolutionProperty(propertyName = "columnList")
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getAscendingD() {
        return (x + y);
    }

    public int getDescendingD() {
        return (x - y);
    }

    public int compareTo(Queen other) {
        return new CompareToBuilder()
                .append(x, other.x)
                .append(y, other.y)
                .append(id, other.id)
                .toComparison();
    }

    public Queen clone() {
        Queen clone = new Queen();
        clone.id = id;
        clone.x = x;
        clone.y = y;
        return clone;
    }

    @Override
    public String toString() {
        return super.toString() + " " + x + " @ " + y;
    }

}
