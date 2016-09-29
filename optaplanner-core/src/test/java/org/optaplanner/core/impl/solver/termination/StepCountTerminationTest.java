/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.solver.termination;

import org.junit.Test;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class StepCountTerminationTest {

    @Test
    public void phaseTermination() {
        Termination termination = new StepCountTermination(4);
        AbstractPhaseScope phaseScope = mock(AbstractPhaseScope.class);

        when(phaseScope.getNextStepIndex()).thenReturn(0);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(false);
        assertEquals(0.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        when(phaseScope.getNextStepIndex()).thenReturn(1);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(false);
        assertEquals(0.25, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        when(phaseScope.getNextStepIndex()).thenReturn(2);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(false);
        assertEquals(0.5, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        when(phaseScope.getNextStepIndex()).thenReturn(3);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(false);
        assertEquals(0.75, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        when(phaseScope.getNextStepIndex()).thenReturn(4);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(true);
        assertEquals(1.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
        when(phaseScope.getNextStepIndex()).thenReturn(5);
        assertThat(termination.isPhaseTerminated(phaseScope)).isEqualTo(true);
        assertEquals(1.0, termination.calculatePhaseTimeGradient(phaseScope), 0.0);
    }

}
