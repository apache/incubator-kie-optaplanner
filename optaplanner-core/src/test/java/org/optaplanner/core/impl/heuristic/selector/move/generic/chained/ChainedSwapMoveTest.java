package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedObject;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ChainedSwapMoveTest {

    @Test
    @Ignore
    public void doMove() {
        EntityDescriptor entityDescriptor = TestdataChainedEntity.buildEntityDescriptor();
        GenuineVariableDescriptor variableDescriptor = entityDescriptor.getGenuineVariableDescriptor("chainedObject");
        InnerScoreDirector scoreDirector = mock(InnerScoreDirector.class);
        TestdataChainedAnchor anchor = new TestdataChainedAnchor("anchor");
        TestdataChainedEntity entity1 = new TestdataChainedEntity("entity1", anchor);
        TestdataChainedEntity entity2 = new TestdataChainedEntity("entity2", entity1);
        TestdataChainedEntity entity3 = new TestdataChainedEntity("entity3", entity2);
        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{entity1, entity2, entity3});
        ChainedSwapMove move = new ChainedSwapMove(
                Collections.singletonList(TestdataChainedEntity.buildVariableDescriptorForChainedObject()), entity1, entity2);
        move.doMove(scoreDirector);
        TestdataChainedEntity startPoint = entity3;

        // TODO: FINISH ME
        //assertEquals(entity2, startPoint.getChainedObject());
        //startPoint = startPoint.getChainedObject();
        //AssertEquals

    }
}
