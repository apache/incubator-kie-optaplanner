/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.domain.lookup;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.domain.lookup.LookUpStrategyType;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.impl.testdata.domain.clone.lookup.TestdataObjectIntegerId;
import org.optaplanner.core.impl.testdata.domain.clone.lookup.TestdataObjectIntegerIdSubclass;
import org.optaplanner.core.impl.testdata.domain.clone.lookup.TestdataObjectMultipleIds;
import org.optaplanner.core.impl.testdata.domain.clone.lookup.TestdataObjectNoId;
import org.optaplanner.core.impl.testdata.domain.clone.lookup.TestdataObjectPrimitiveIntId;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class LookUpStrategyIdOrNoneTest {

    private LookUpManager lookUpManager;

    @BeforeEach
    public void setUpLookUpManager() {
        lookUpManager = new LookUpManager(new LookUpStrategyResolver(LookUpStrategyType.PLANNING_ID_OR_NONE));
        lookUpManager.resetWorkingObjects(Collections.emptyList());
    }

    @Test
    public void addRemoveWithIntegerId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
        // The removed object cannot be looked up
        assertNull(lookUpManager.lookUpWorkingObjectOrReturnNull(object));
    }

    @Test
    public void addRemoveWithPrimitiveIntId() {
        TestdataObjectPrimitiveIntId object = new TestdataObjectPrimitiveIntId(0);
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
        // The removed object cannot be looked up
        assertNull(lookUpManager.lookUpWorkingObjectOrReturnNull(object));
    }

    @Test
    public void addWithNullIdInSuperclass() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerIdSubclass(null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.addWorkingObject(object))
                .withMessageContaining("must not be null")
                .withMessageContaining(TestdataObjectIntegerIdSubclass.class.getCanonicalName())
                .withMessageContaining(object.toString());
    }

    @Test
    public void removeWithNullId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.removeWorkingObject(object))
                .withMessageContaining("must not be null");
    }

    @Test
    public void addWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        lookUpManager.addWorkingObject(object);
    }

    @Test
    public void removeWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    public void addSameIdTwice() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(2);
        lookUpManager.addWorkingObject(object);
        assertThatIllegalStateException()
                .isThrownBy(() -> lookUpManager.addWorkingObject(new TestdataObjectIntegerId(2)))
                .withMessageContaining(" have the same planningId ")
                .withMessageContaining(object.toString());
    }

    @Test
    public void removeWithoutAdding() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        assertThatIllegalStateException()
                .isThrownBy(() -> lookUpManager.removeWorkingObject(object))
                .withMessageContaining("differ");
    }

    @Test
    public void lookUpWithId() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(1);
        lookUpManager.addWorkingObject(object);
        assertSame(object, lookUpManager.lookUpWorkingObject(new TestdataObjectIntegerId(1)));
    }

    @Test
    public void lookUpWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        lookUpManager.addWorkingObject(object);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.lookUpWorkingObject(object))
                .withMessageContaining("cannot be looked up");
    }

    @Test
    public void lookUpWithoutAdding() {
        TestdataObjectIntegerId object = new TestdataObjectIntegerId(0);
        assertNull(lookUpManager.lookUpWorkingObjectOrReturnNull(object));
    }

    @Test
    public void addWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.addWorkingObject(object))
                .withMessageContaining("3 members")
                .withMessageContaining(PlanningId.class.getSimpleName());
    }

    @Test
    public void removeWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> lookUpManager.removeWorkingObject(object))
                .withMessageContaining("3 members")
                .withMessageContaining(PlanningId.class.getSimpleName());
    }
}
