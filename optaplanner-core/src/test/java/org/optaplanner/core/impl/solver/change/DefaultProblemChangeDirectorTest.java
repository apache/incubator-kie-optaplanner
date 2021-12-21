/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.solver.change;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.api.solver.change.ProblemChangeDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;

public class DefaultProblemChangeDirectorTest {

    @Test
    void complexProblemChange_correctlyNotifiesScoreDirector() {
        final TestdataEntity addedEntity = new TestdataEntity("newly added entity");
        final TestdataEntity removedEntity = new TestdataEntity("entity to remove");
        final TestdataValue addedFact = new TestdataValue("newly added fact");
        final TestdataValue removedFact = new TestdataValue("fact to remove");
        final TestdataEntity changedEntity = new TestdataEntity("changed entity");
        final TestdataValue changedEntityValue = new TestdataValue("changed entity value");

        ScoreDirector<TestdataSolution> scoreDirectorMock = mock(ScoreDirector.class);
        when(scoreDirectorMock.lookUpWorkingObject(removedEntity)).thenReturn(removedEntity);
        when(scoreDirectorMock.lookUpWorkingObject(changedEntity)).thenReturn(changedEntity);
        when(scoreDirectorMock.lookUpWorkingObject(removedFact)).thenReturn(removedFact);
        ProblemChangeDirector defaultProblemChangeDirector = new DefaultProblemChangeDirector<>(scoreDirectorMock);

        ProblemChange<TestdataSolution> problemChange = ((workingSolution, problemChangeDirector) -> {
            // Add an entity.
            problemChangeDirector.addEntity(addedEntity, workingSolution.getEntityList()::add);
            // Remove an entity.
            problemChangeDirector.removeEntity(removedEntity, workingSolution.getEntityList()::remove);
            // Change planning variable.
            problemChangeDirector.changeVariable(changedEntity, TestdataEntity.VALUE_FIELD,
                    testdataEntity -> testdataEntity.setValue(changedEntityValue));
            // Add a problem fact.
            problemChangeDirector.addProblemFact(addedFact, workingSolution.getValueList()::add);
            // Remove a problem fact.
            problemChangeDirector.removeProblemFact(removedFact, workingSolution.getValueList()::remove);
        });

        TestdataSolution testdataSolution = TestdataSolution.generateSolution();
        testdataSolution.getEntityList().add(removedEntity);
        testdataSolution.getEntityList().add(changedEntity);
        testdataSolution.getValueList().add(removedFact);
        testdataSolution.getValueList().add(changedEntityValue);

        problemChange.doChange(testdataSolution, defaultProblemChangeDirector);
        verify(scoreDirectorMock, times(1)).beforeEntityAdded(addedEntity);
        verify(scoreDirectorMock, times(1)).afterEntityAdded(addedEntity);

        verify(scoreDirectorMock, times(1)).beforeEntityRemoved(removedEntity);
        verify(scoreDirectorMock, times(1)).afterEntityRemoved(removedEntity);

        verify(scoreDirectorMock, times(1))
                .beforeVariableChanged(changedEntity, TestdataEntity.VALUE_FIELD);
        verify(scoreDirectorMock, times(1))
                .afterVariableChanged(changedEntity, TestdataEntity.VALUE_FIELD);

        verify(scoreDirectorMock, times(1)).beforeProblemFactAdded(addedFact);
        verify(scoreDirectorMock, times(1)).afterProblemFactAdded(addedFact);

        verify(scoreDirectorMock, times(1)).beforeProblemFactRemoved(removedFact);
        verify(scoreDirectorMock, times(1)).afterProblemFactRemoved(removedFact);
    }
}
