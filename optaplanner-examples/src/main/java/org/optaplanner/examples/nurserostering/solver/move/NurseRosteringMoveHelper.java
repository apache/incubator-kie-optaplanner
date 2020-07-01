/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.nurserostering.solver.move;

import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.examples.nurserostering.domain.Employee;
import org.optaplanner.examples.nurserostering.domain.NurseRoster;
import org.optaplanner.examples.nurserostering.domain.ShiftAssignment;

public class NurseRosteringMoveHelper {

    public static void moveEmployee(ScoreDirector<NurseRoster> scoreDirector, ShiftAssignment shiftAssignment,
            Employee toEmployee) {
        scoreDirector.beforeVariableChanged(shiftAssignment, "employee");
        shiftAssignment.setEmployee(toEmployee);
        scoreDirector.afterVariableChanged(shiftAssignment, "employee");
    }

    private NurseRosteringMoveHelper() {
    }

}
