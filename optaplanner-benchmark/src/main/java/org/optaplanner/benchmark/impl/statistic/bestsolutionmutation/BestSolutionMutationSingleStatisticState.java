/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.optaplanner.benchmark.impl.statistic.bestsolutionmutation;

import java.util.ArrayList;
import java.util.List;
import org.optaplanner.benchmark.impl.statistic.SingleStatisticState;

/**
 *
 * @author matej
 */
public class BestSolutionMutationSingleStatisticState implements SingleStatisticState {
    
    private List<BestSolutionMutationSingleStatisticPoint> pointList = new ArrayList<BestSolutionMutationSingleStatisticPoint>();

    public List<BestSolutionMutationSingleStatisticPoint> getPointList() {
        return pointList;
    }

    public void setPointList(List<BestSolutionMutationSingleStatisticPoint> pointList) {
        this.pointList = pointList;
    }
}
