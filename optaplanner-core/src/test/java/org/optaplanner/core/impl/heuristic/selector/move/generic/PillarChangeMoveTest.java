/*
 * Copyright 2014 JBoss Inc
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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.entityproviding.TestdataEntityProvidingEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataOtherValue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PillarChangeMoveTest {

    @Test
    public void isMoveDoableValueRangeProviderOnEntity() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        PillarChangeMove abMove;
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b),
                entityDescriptor.getGenuineVariableDescriptor("value"), v1);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b),
                entityDescriptor.getGenuineVariableDescriptor("value"), v2);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b),
                entityDescriptor.getGenuineVariableDescriptor("value"), v3);
        assertEquals(true, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        abMove = new PillarChangeMove(Arrays.<Object>asList(a, b),
                entityDescriptor.getGenuineVariableDescriptor("value"), v4);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        PillarChangeMove abMove = new PillarChangeMove(Arrays.<Object>asList(a, b),
                entityDescriptor.getGenuineVariableDescriptor("value"), v2);

        a.setValue(v3);
        b.setValue(v3);
        abMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v2, b.getValue());

        a.setValue(v2);
        b.setValue(v2);
        abMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v2, b.getValue());

        PillarChangeMove abcMove = new PillarChangeMove(Arrays.<Object>asList(a, b, c),
                entityDescriptor.getGenuineVariableDescriptor("value"), v2);

        a.setValue(v2);
        b.setValue(v2);
        c.setValue(v2);
        abcMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v2, b.getValue());
        assertEquals(v2, c.getValue());

        a.setValue(v3);
        b.setValue(v3);
        c.setValue(v3);
        abcMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v2, b.getValue());
        assertEquals(v2, c.getValue());
    }

    @Test
    public void getPlanningEntities() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v1), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v1), null);
        PillarChangeMove move = new PillarChangeMove(Arrays.<Object>asList(a, b, c), null, null);
        List<Object> entities = (List<Object>) move.getPlanningEntities();
        assertEquals(3, entities.size());
        assertEquals(true, entities.contains(a));
        assertEquals(true, entities.contains(b));
        assertEquals(true, entities.contains(c));
    }

    @Test
    public void getPlanningValues() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataMultiVarEntity a = new TestdataMultiVarEntity("a", v2, v2, new TestdataOtherValue());
        TestdataMultiVarEntity b = new TestdataMultiVarEntity("b", v2, v2, new TestdataOtherValue());
        TestdataMultiVarEntity c = new TestdataMultiVarEntity("c", v2, v3, new TestdataOtherValue());
        PillarChangeMove move = new PillarChangeMove(Arrays.<Object>asList(a, b, c), null, v1);

        List<Object> values = (List<Object>) move.getPlanningValues();
        assertEquals(1, values.size());
        assertEquals(true, values.contains(v1));
    }

}
