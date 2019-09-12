/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.pas.domain.solver;

import java.io.Serializable;
import java.util.Comparator;

import org.optaplanner.core.api.score.constraint.ConstraintJustification;
import org.optaplanner.examples.pas.domain.Bed;
import org.optaplanner.examples.pas.domain.Department;
import org.optaplanner.examples.pas.domain.Room;

public class BedStrengthComparator implements Comparator<Bed>,
        Serializable {

    private static final Comparator<Department> DEPARTMENT_COMPARATOR =
            Comparator.comparing((Department department) -> department.getMinimumAge() == null) // null minimumAge is stronger
                    .thenComparing(department -> department.getMaximumAge() == null) // null maximumAge is stronger
                    .thenComparingInt(department -> -department.getMinimumAge()) // Descending, low minimumAge is stronger
                    .thenComparingInt(Department::getMaximumAge); // High maximumAge is stronger
    private static final Comparator<Room> ROOM_COMPARATOR =
            Comparator.comparingInt((Room room) -> room.getRoomEquipmentList().size())
                    .thenComparingInt(room -> room.getRoomSpecialismList().size())
                    .thenComparingInt(room -> -room.getCapacity()); // Descending (smaller rooms are stronger)
    private static final Comparator<Bed> COMPARATOR =
            Comparator.comparing((Bed bed) -> bed.getRoom().getDepartment(), DEPARTMENT_COMPARATOR)
                    .thenComparing(Bed::getRoom, ROOM_COMPARATOR)
                    .thenComparing(ConstraintJustification.COMPARATOR);

    @Override
    public int compare(Bed a, Bed b) {
        return COMPARATOR.compare(a, b);
    }
}
