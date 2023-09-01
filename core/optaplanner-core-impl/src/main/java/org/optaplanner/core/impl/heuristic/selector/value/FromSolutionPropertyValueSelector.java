/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value;

import java.util.Iterator;
import java.util.Objects;

import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.impl.domain.valuerange.descriptor.EntityIndependentValueRangeDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.AbstractDemandEnabledSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

/**
 * This is the common {@link ValueSelector} implementation.
 */
public final class FromSolutionPropertyValueSelector<Solution_>
        extends AbstractDemandEnabledSelector<Solution_>
        implements EntityIndependentValueSelector<Solution_> {

    private final EntityIndependentValueRangeDescriptor<Solution_> valueRangeDescriptor;
    private final SelectionCacheType minimumCacheType;
    private final boolean randomSelection;
    private final boolean valueRangeMightContainEntity;

    private ValueRange<Object> cachedValueRange = null;
    private Long cachedEntityListRevision = null;
    private boolean cachedEntityListIsDirty = false;

    public FromSolutionPropertyValueSelector(EntityIndependentValueRangeDescriptor<Solution_> valueRangeDescriptor,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        this.valueRangeDescriptor = valueRangeDescriptor;
        this.minimumCacheType = minimumCacheType;
        this.randomSelection = randomSelection;
        valueRangeMightContainEntity = valueRangeDescriptor.mightContainEntity();
    }

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return valueRangeDescriptor.getVariableDescriptor();
    }

    @Override
    public SelectionCacheType getCacheType() {
        SelectionCacheType intrinsicCacheType = valueRangeMightContainEntity
                ? SelectionCacheType.STEP
                : SelectionCacheType.PHASE;
        return (intrinsicCacheType.compareTo(minimumCacheType) > 0)
                ? intrinsicCacheType
                : minimumCacheType;
    }

    // ************************************************************************
    // Cache lifecycle methods
    // ************************************************************************

    @Override
    public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        InnerScoreDirector<Solution_, ?> scoreDirector = phaseScope.getScoreDirector();
        cachedValueRange = (ValueRange<Object>) valueRangeDescriptor.extractValueRange(scoreDirector.getWorkingSolution());
        if (valueRangeMightContainEntity) {
            cachedEntityListRevision = scoreDirector.getWorkingEntityListRevision();
            cachedEntityListIsDirty = false;
        }
    }

    @Override
    public void stepStarted(AbstractStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        if (valueRangeMightContainEntity) {
            InnerScoreDirector<Solution_, ?> scoreDirector = stepScope.getScoreDirector();
            if (scoreDirector.isWorkingEntityListDirty(cachedEntityListRevision)) {
                if (minimumCacheType.compareTo(SelectionCacheType.STEP) > 0) {
                    cachedEntityListIsDirty = true;
                } else {
                    cachedValueRange = (ValueRange<Object>) valueRangeDescriptor
                            .extractValueRange(scoreDirector.getWorkingSolution());
                    cachedEntityListRevision = scoreDirector.getWorkingEntityListRevision();
                }
            }
        }
    }

    @Override
    public void phaseEnded(AbstractPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        cachedValueRange = null;
        if (valueRangeMightContainEntity) {
            cachedEntityListRevision = null;
            cachedEntityListIsDirty = false;
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return valueRangeDescriptor.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || !isCountable();
    }

    @Override
    public long getSize(Object entity) {
        return getSize();
    }

    @Override
    public long getSize() {
        return ((CountableValueRange<?>) cachedValueRange).getSize();
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        return iterator();
    }

    @Override
    public Iterator<Object> iterator() {
        checkCachedEntityListIsDirty();
        if (randomSelection) {
            return cachedValueRange.createRandomIterator(workingRandom);
        }
        if (cachedValueRange instanceof CountableValueRange) {
            return ((CountableValueRange<Object>) cachedValueRange).createOriginalIterator();
        }
        throw new IllegalStateException("Value range's class (" + cachedValueRange.getClass().getCanonicalName() + ") " +
                "does not implement " + CountableValueRange.class + ", " +
                "yet selectionOrder is not " + SelectionOrder.RANDOM + ".\n" +
                "Maybe switch selectors' selectionOrder to " + SelectionOrder.RANDOM + "?\n" +
                "Maybe switch selectors' cacheType to " + SelectionCacheType.JUST_IN_TIME + "?");
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        return endingIterator();
    }

    public Iterator<Object> endingIterator() {
        return ((CountableValueRange<Object>) cachedValueRange).createOriginalIterator();
    }

    private void checkCachedEntityListIsDirty() {
        if (cachedEntityListIsDirty) {
            throw new IllegalStateException("The selector (" + this + ") with minimumCacheType (" + minimumCacheType
                    + ")'s workingEntityList became dirty between steps but is still used afterwards.");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        FromSolutionPropertyValueSelector<?> that = (FromSolutionPropertyValueSelector<?>) other;
        return randomSelection == that.randomSelection &&
                Objects.equals(valueRangeDescriptor, that.valueRangeDescriptor) && minimumCacheType == that.minimumCacheType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueRangeDescriptor, minimumCacheType, randomSelection);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getVariableDescriptor().getVariableName() + ")";
    }

}
