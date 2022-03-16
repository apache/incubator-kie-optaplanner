/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.variable.listener.support;

import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

abstract class AbstractNotificationTest {

    private final String object = "Testing object";

    @Test
    void testTriggerBefore(@Mock VariableListener<Object, Object> variableListener,
            @Mock ScoreDirector<Object> scoreDirector) {
        VariableListenerNotification notification = createNotificationInstance(object);

        notification.triggerBefore(variableListener, scoreDirector);
        callInteractionBefore(verify(variableListener), same(scoreDirector), same(object));
        verifyNoMoreInteractions(variableListener);
    }

    protected abstract VariableListenerNotification createNotificationInstance(Object object);

    protected abstract void callInteractionBefore(VariableListener<Object, Object> variableListener,
            ScoreDirector<Object> scoreDirector, Object object);

    @Test
    void testTriggerAfter(@Mock VariableListener<Object, Object> variableListener,
            @Mock ScoreDirector<Object> scoreDirector) {
        VariableListenerNotification notification = createNotificationInstance(object);

        notification.triggerAfter(variableListener, scoreDirector);
        callInteractionAfter(verify(variableListener), same(scoreDirector), same(object));
        verifyNoMoreInteractions(variableListener);
    }

    protected abstract void callInteractionAfter(VariableListener<Object, Object> variableListener,
            ScoreDirector<Object> scoreDirector, Object object);

    @Test
    void testEquality() {
        VariableListenerNotification notification = createNotificationInstance(object);
        Assertions.assertThat(notification).isEqualTo(notification);

        VariableListenerNotification notification2 = createNotificationInstance(object);
        assertSoftly(softly -> {
            softly.assertThat(notification).isEqualTo(notification2);
            softly.assertThat(notification2).isEqualTo(notification);
            softly.assertThat(notification).isNotSameAs(notification2);
        });

        VariableListenerNotification notification3 = createNotificationInstance("Some other object");
        assertSoftly(softly -> {
            softly.assertThat(notification).isNotEqualTo(notification3);
            softly.assertThat(notification3).isNotEqualTo(notification);
        });
    }

}
