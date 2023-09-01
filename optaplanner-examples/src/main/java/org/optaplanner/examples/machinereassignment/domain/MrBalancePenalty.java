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

package org.optaplanner.examples.machinereassignment.domain;

import org.optaplanner.examples.common.domain.AbstractPersistable;

public class MrBalancePenalty extends AbstractPersistable {

    private MrResource originResource;
    private MrResource targetResource;
    private int multiplicand;
    private int weight;

    @SuppressWarnings("unused")
    MrBalancePenalty() {
    }

    public MrBalancePenalty(MrResource originResource, MrResource targetResource, int multiplicand, int weight) {
        this.originResource = originResource;
        this.targetResource = targetResource;
        this.multiplicand = multiplicand;
        this.weight = weight;
    }

    public MrBalancePenalty(long id, MrResource originResource, MrResource targetResource, int multiplicand, int weight) {
        super(id);
        this.originResource = originResource;
        this.targetResource = targetResource;
        this.multiplicand = multiplicand;
        this.weight = weight;
    }

    public MrResource getOriginResource() {
        return originResource;
    }

    public MrResource getTargetResource() {
        return targetResource;
    }

    public int getMultiplicand() {
        return multiplicand;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
