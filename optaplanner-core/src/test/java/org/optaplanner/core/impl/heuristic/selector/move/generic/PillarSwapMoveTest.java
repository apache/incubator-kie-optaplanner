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

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.entityproviding.TestdataEntityProvidingEntity;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PillarSwapMoveTest {

    @Test
    public void isMoveDoableValueRangeProviderOnEntity() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v4, v5), null);
        TestdataEntityProvidingEntity z = new TestdataEntityProvidingEntity("z", Arrays.asList(v1, v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        PillarSwapMove abMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a), Arrays.<Object>asList(b));
        a.setValue(v1);
        b.setValue(v2);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v3);
        assertEquals(true, abMove.isMoveDoable(scoreDirector));
        a.setValue(v3);
        b.setValue(v2);
        assertEquals(true, abMove.isMoveDoable(scoreDirector));
        a.setValue(v3);
        b.setValue(v3);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v4);
        assertEquals(false, abMove.isMoveDoable(scoreDirector));

        PillarSwapMove acMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a), Arrays.<Object>asList(c));
        a.setValue(v1);
        c.setValue(v4);
        assertEquals(false, acMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        c.setValue(v5);
        assertEquals(false, acMove.isMoveDoable(scoreDirector));

        PillarSwapMove bcMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(b), Arrays.<Object>asList(c));
        b.setValue(v2);
        c.setValue(v4);
        assertEquals(false, bcMove.isMoveDoable(scoreDirector));
        b.setValue(v4);
        c.setValue(v5);
        assertEquals(true, bcMove.isMoveDoable(scoreDirector));
        b.setValue(v5);
        c.setValue(v4);
        assertEquals(true, bcMove.isMoveDoable(scoreDirector));
        b.setValue(v5);
        c.setValue(v5);
        assertEquals(false, bcMove.isMoveDoable(scoreDirector));

        PillarSwapMove abzMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a, b), Arrays.<Object>asList(z));
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v4);
        assertEquals(false, abzMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v1);
        assertEquals(false, abzMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v3);
        assertEquals(true, abzMove.isMoveDoable(scoreDirector));
        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v2);
        assertEquals(true, abzMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v2);
        assertEquals(false, abzMove.isMoveDoable(scoreDirector));
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3, v4), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v4, v5), null);
        TestdataEntityProvidingEntity z = new TestdataEntityProvidingEntity("z", Arrays.asList(v1, v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();

        PillarSwapMove abMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a), Arrays.<Object>asList(b));

        a.setValue(v1);
        b.setValue(v1);
        abMove.doMove(scoreDirector);
        assertEquals(v1, a.getValue());
        assertEquals(v1, b.getValue());

        a.setValue(v2);
        b.setValue(v1);
        abMove.doMove(scoreDirector);
        assertEquals(v1, a.getValue());
        assertEquals(v2, b.getValue());

        a.setValue(v3);
        b.setValue(v2);
        abMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v3, b.getValue());
        abMove.doMove(scoreDirector);
        assertEquals(v3, a.getValue());
        assertEquals(v2, b.getValue());

        PillarSwapMove abzMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a, b), Arrays.<Object>asList(z));

        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v2);
        abzMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v2, b.getValue());
        assertEquals(v3, z.getValue());
        abzMove.doMove(scoreDirector);
        assertEquals(v3, a.getValue());
        assertEquals(v3, b.getValue());
        assertEquals(v2, z.getValue());

        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v4);
        abzMove.doMove(scoreDirector);
        assertEquals(v4, a.getValue());
        assertEquals(v4, b.getValue());
        assertEquals(v3, z.getValue());
        abzMove.doMove(scoreDirector);
        assertEquals(v3, a.getValue());
        assertEquals(v3, b.getValue());
        assertEquals(v4, z.getValue());

        PillarSwapMove abczMove = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a), Arrays.<Object>asList(b, c, z));

        a.setValue(v2);
        b.setValue(v3);
        c.setValue(v3);
        z.setValue(v3);
        abczMove.doMove(scoreDirector);
        assertEquals(v3, a.getValue());
        assertEquals(v2, b.getValue());
        assertEquals(v2, c.getValue());
        assertEquals(v2, z.getValue());
        abczMove.doMove(scoreDirector);
        assertEquals(v2, a.getValue());
        assertEquals(v3, b.getValue());
        assertEquals(v3, c.getValue());
        assertEquals(v3, z.getValue());

        PillarSwapMove abczMove2 = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(),
                Arrays.<Object>asList(a, b), Arrays.<Object>asList(c, z));

        a.setValue(v4);
        b.setValue(v4);
        c.setValue(v3);
        z.setValue(v3);
        abczMove2.doMove(scoreDirector);
        assertEquals(v3, a.getValue());
        assertEquals(v3, b.getValue());
        assertEquals(v4, c.getValue());
        assertEquals(v4, z.getValue());
        abczMove2.doMove(scoreDirector);
        assertEquals(v4, a.getValue());
        assertEquals(v4, b.getValue());
        assertEquals(v3, c.getValue());
        assertEquals(v3, z.getValue());

    }

    @Test
    public void getPlanningEntities() {
        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(new TestdataValue("1")), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(new TestdataValue("1")), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(new TestdataValue("1")), null);
        TestdataEntityProvidingEntity d = new TestdataEntityProvidingEntity("d", Arrays.asList(new TestdataValue("1")), null);

        EntityDescriptor entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();
        PillarSwapMove move = new PillarSwapMove(entityDescriptor.getGenuineVariableDescriptors(), Arrays.asList((Object)a, b), Arrays.asList((Object)c, d));
        List<Object> entities = (List<Object>) move.getPlanningEntities();

        assertEquals(4, entities.size());
        assertEquals(true, entities.contains(a));
        assertEquals(true, entities.contains(b));
        assertEquals(true, entities.contains(c));
        assertEquals(true, entities.contains(d));
    }

    @Test
    @Ignore("Waiting for fix. For now, this test is irrelevant. Implement test after bug is fixed") // https://issues.jboss.org/browse/PLANNER-278
    public void getPlanningValues() {
        // TODO: after bug is fixed write proper test for it.
    }

}
