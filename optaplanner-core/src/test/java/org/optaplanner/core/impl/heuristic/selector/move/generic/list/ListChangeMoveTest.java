/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessorFactory;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;

class ListChangeMoveTest {

    @Test
    void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataListEntity e1 = new TestdataListEntity(asList(v1, v2));
        TestdataListEntity e2 = new TestdataListEntity(asList(v3));

        ScoreDirector<TestdataSolution> scoreDirector = mock(ScoreDirector.class);
        DefaultListVariableDescriptor<?> variableDescriptor = buildVariableDescriptor();
        ListChangeMove<TestdataSolution> move = new ListChangeMove<>(e1, 1, e2, 1, variableDescriptor);

        AbstractMove<TestdataSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1);
        assertThat(e2.getValueList()).containsExactly(v3, v2);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2);
        assertThat(e2.getValueList()).containsExactly(v3);
    }

    @Test
    void isMoveDoable() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataListEntity e1 = new TestdataListEntity(asList(v1, v2));
        TestdataListEntity e2 = new TestdataListEntity(asList(v3));

        ScoreDirector<TestdataSolution> scoreDirector = mock(ScoreDirector.class);
        DefaultListVariableDescriptor<?> variableDescriptor = buildVariableDescriptor();

        // same entity, same index => not doable because the move doesn't change anything
        assertThat(new ListChangeMove<TestdataSolution>(e1, 1, e1, 1, variableDescriptor).isMoveDoable(scoreDirector))
                .isFalse();
        // same entity, different index => doable
        assertThat(new ListChangeMove<TestdataSolution>(e1, 0, e1, 1, variableDescriptor).isMoveDoable(scoreDirector))
                .isTrue();
        // different entity => doable
        assertThat(new ListChangeMove<TestdataSolution>(e1, 0, e2, 0, variableDescriptor).isMoveDoable(scoreDirector))
                .isTrue();
    }

    // TODO replace this with TestdataListEntity.buildEntityDescriptor()
    private static DefaultListVariableDescriptor<?> buildVariableDescriptor() {
        MemberAccessor memberAccessor = null;
        try {
            memberAccessor = MemberAccessorFactory.buildMemberAccessor(
                    TestdataListEntity.class.getDeclaredField("valueList"),
                    MemberAccessorFactory.MemberAccessorType.FIELD_OR_GETTER_METHOD,
                    null, // @PlanningCollectionVariable
                    DomainAccessType.REFLECTION,
                    Collections.emptyMap());
        } catch (NoSuchFieldException e) {
            fail("No such field", e);
        }
        return new DefaultListVariableDescriptor<>(null, memberAccessor);
    }

    private static List<TestdataValue> asList(TestdataValue... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
