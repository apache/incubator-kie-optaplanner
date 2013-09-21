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
package org.optaplanner.benchmark.impl.statistic.bestscore;

import java.util.ArrayList;
import java.util.List;
import org.optaplanner.benchmark.impl.statistic.SingleStatisticState;

public class BestScoreSingleStatisticState implements SingleStatisticState {
    
    private List<BestScoreSingleStatisticPoint> pointList = new ArrayList<BestScoreSingleStatisticPoint>();

    public List<BestScoreSingleStatisticPoint> getPointList() {
        return pointList;
    }

    public void setPointList(List<BestScoreSingleStatisticPoint> pointList) {
        this.pointList = pointList;
    }
}
