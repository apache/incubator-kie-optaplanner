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

package org.optaplanner.examples.tennis.optional.score;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.examples.tennis.domain.TeamAssignment;

public class TennisConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {

        };
    }

    // ############################################################################
    // Hard constraints
    // ############################################################################

    private Constraint oneAssignmentPerDatePerTeam(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TeamAssignment.class)
                .ifExistsOther(TeamAssignment.class,
                        Joiners.equal(TeamAssignment::getTeam),
                        Joiners.equal(TeamAssignment::getDay),
                        Joiners.lessThan(TeamAssignment::getId),
                        Joiners.filtering((teamAssignment, otherTeamAssignment) -> teamAssignment.getTeam() != null))
                .penalize("oneAssignmentPerDatePerTeam", HardMediumSoftScore.ONE_HARD);
    }
}
