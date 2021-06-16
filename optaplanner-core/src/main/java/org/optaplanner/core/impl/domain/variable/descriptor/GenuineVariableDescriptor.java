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

package org.optaplanner.core.impl.domain.variable.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningCollectionVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.valuerange.descriptor.CompositeValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.FromEntityPropertyValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.FromSolutionPropertyValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.ValueRangeDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.ComparatorSelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.NullValueReinitializeVariableEntityFilter;
import org.optaplanner.core.impl.heuristic.selector.value.decorator.MovableChainedTrailingValueFilter;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class GenuineVariableDescriptor<Solution_> extends VariableDescriptor<Solution_> {

    private ValueRangeDescriptor<Solution_> valueRangeDescriptor;
    private SelectionFilter<Solution_, Object> movableChainedTrailingValueFilter;
    private SelectionFilter<Solution_, Object> reinitializeVariableEntityFilter =
            new NullValueReinitializeVariableEntityFilter<>(this);
    private SelectionSorter<Solution_, Object> increasingStrengthSorter;
    private SelectionSorter<Solution_, Object> decreasingStrengthSorter;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public GenuineVariableDescriptor(EntityDescriptor<Solution_> entityDescriptor,
            MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    public void processAnnotations(DescriptorPolicy descriptorPolicy) {
        processPropertyAnnotations(descriptorPolicy);
    }

    abstract protected void processPropertyAnnotations(DescriptorPolicy descriptorPolicy);

    protected void processValueRangeRefs(DescriptorPolicy descriptorPolicy, Supplier<String[]> valueRangeProviderRefsSupplier) {
        String[] valueRangeProviderRefs = valueRangeProviderRefsSupplier.get();
        if (ArrayUtils.isEmpty(valueRangeProviderRefs)) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + PlanningVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") that has no valueRangeProviderRefs (" + Arrays.toString(valueRangeProviderRefs) + ").");
        }
        List<ValueRangeDescriptor<Solution_>> valueRangeDescriptorList = new ArrayList<>(valueRangeProviderRefs.length);
        boolean addNullInValueRange = isNullable() && valueRangeProviderRefs.length == 1;
        for (String valueRangeProviderRef : valueRangeProviderRefs) {
            valueRangeDescriptorList
                    .add(buildValueRangeDescriptor(descriptorPolicy, valueRangeProviderRef, addNullInValueRange));
        }
        if (valueRangeDescriptorList.size() == 1) {
            valueRangeDescriptor = valueRangeDescriptorList.get(0);
        } else {
            valueRangeDescriptor = new CompositeValueRangeDescriptor<>(this, isNullable(), valueRangeDescriptorList);
        }
    }

    private ValueRangeDescriptor<Solution_> buildValueRangeDescriptor(DescriptorPolicy descriptorPolicy,
            String valueRangeProviderRef, boolean addNullInValueRange) {
        if (descriptorPolicy.hasFromSolutionValueRangeProvider(valueRangeProviderRef)) {
            MemberAccessor memberAccessor = descriptorPolicy.getFromSolutionValueRangeProvider(valueRangeProviderRef);
            return new FromSolutionPropertyValueRangeDescriptor<>(this, addNullInValueRange, memberAccessor);
        } else if (descriptorPolicy.hasFromEntityValueRangeProvider(valueRangeProviderRef)) {
            MemberAccessor memberAccessor = descriptorPolicy.getFromEntityValueRangeProvider(valueRangeProviderRef);
            return new FromEntityPropertyValueRangeDescriptor<>(this, addNullInValueRange, memberAccessor);
        } else {
            Collection<String> providerIds = descriptorPolicy.getValueRangeProviderIds();
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + PlanningVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") with a valueRangeProviderRef (" + valueRangeProviderRef
                    + ") that does not exist in a @" + ValueRangeProvider.class.getSimpleName()
                    + " on the solution class ("
                    + entityDescriptor.getSolutionDescriptor().getSolutionClass().getSimpleName()
                    + ") or on that entityClass.\n"
                    + "The valueRangeProviderRef (" + valueRangeProviderRef
                    + ") does not appear in the valueRangeProvideIds (" + providerIds
                    + ")." + (!providerIds.isEmpty() ? ""
                            : "\nMaybe a @" + ValueRangeProvider.class.getSimpleName()
                                    + " annotation is missing on a method in the solution class ("
                                    + entityDescriptor.getSolutionDescriptor().getSolutionClass().getSimpleName() + ")."));
        }
    }

    protected void processStrength(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        Class<? extends Comparator> strengthComparatorClass = planningVariableAnnotation.strengthComparatorClass();
        if (strengthComparatorClass == PlanningVariable.NullStrengthComparator.class) {
            strengthComparatorClass = null;
        }
        Class<? extends SelectionSorterWeightFactory> strengthWeightFactoryClass = planningVariableAnnotation
                .strengthWeightFactoryClass();
        if (strengthWeightFactoryClass == PlanningVariable.NullStrengthWeightFactory.class) {
            strengthWeightFactoryClass = null;
        }
        if (strengthComparatorClass != null && strengthWeightFactoryClass != null) {
            throw new IllegalStateException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") property (" + variableMemberAccessor.getName()
                    + ") cannot have a strengthComparatorClass (" + strengthComparatorClass.getName()
                    + ") and a strengthWeightFactoryClass (" + strengthWeightFactoryClass.getName()
                    + ") at the same time.");
        }
        if (strengthComparatorClass != null) {
            Comparator<Object> strengthComparator = ConfigUtils.newInstance(this::toString,
                    "strengthComparatorClass", strengthComparatorClass);
            increasingStrengthSorter = new ComparatorSelectionSorter<>(strengthComparator,
                    SelectionSorterOrder.ASCENDING);
            decreasingStrengthSorter = new ComparatorSelectionSorter<>(strengthComparator,
                    SelectionSorterOrder.DESCENDING);
        }
        if (strengthWeightFactoryClass != null) {
            SelectionSorterWeightFactory<Solution_, Object> strengthWeightFactory = ConfigUtils.newInstance(this::toString,
                    "strengthWeightFactoryClass", strengthWeightFactoryClass);
            increasingStrengthSorter = new WeightFactorySelectionSorter<>(strengthWeightFactory,
                    SelectionSorterOrder.ASCENDING);
            decreasingStrengthSorter = new WeightFactorySelectionSorter<>(strengthWeightFactory,
                    SelectionSorterOrder.DESCENDING);
        }
    }

    @Override
    public void linkVariableDescriptors(DescriptorPolicy descriptorPolicy) {
        if (isChained() && entityDescriptor.hasEffectiveMovableEntitySelectionFilter()) {
            movableChainedTrailingValueFilter = new MovableChainedTrailingValueFilter<>(this);
        } else {
            movableChainedTrailingValueFilter = null;
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public abstract boolean isListVariable();

    public abstract boolean isChained();

    public abstract boolean isNullable();

    public abstract boolean acceptsValueType(Class<?> valueType);

    public boolean hasMovableChainedTrailingValueFilter() {
        return movableChainedTrailingValueFilter != null;
    }

    public SelectionFilter<Solution_, Object> getMovableChainedTrailingValueFilter() {
        return movableChainedTrailingValueFilter;
    }

    public SelectionFilter<Solution_, Object> getReinitializeVariableEntityFilter() {
        return reinitializeVariableEntityFilter;
    }

    public ValueRangeDescriptor<Solution_> getValueRangeDescriptor() {
        return valueRangeDescriptor;
    }

    public boolean isValueRangeEntityIndependent() {
        return valueRangeDescriptor.isEntityIndependent();
    }

    // ************************************************************************
    // Extraction methods
    // ************************************************************************

    /**
     * A {@link PlanningVariable#nullable() nullable} planning variable and {@link PlanningCollectionVariable}
     * are always considered initialized.
     *
     * @param entity never null
     * @return true if the variable on that entity is initialized
     */
    public abstract boolean isInitialized(Object entity);

    @Override
    public boolean isGenuineAndUninitialized(Object entity) {
        return !isInitialized(entity);
    }

    public boolean isReinitializable(ScoreDirector<Solution_> scoreDirector, Object entity) {
        return reinitializeVariableEntityFilter.accept(scoreDirector, entity);
    }

    public SelectionSorter<Solution_, Object> getIncreasingStrengthSorter() {
        return increasingStrengthSorter;
    }

    public SelectionSorter<Solution_, Object> getDecreasingStrengthSorter() {
        return decreasingStrengthSorter;
    }

    public long getValueCount(Solution_ solution, Object entity) {
        if (!valueRangeDescriptor.isCountable()) {
            // TODO report this better than just ignoring it
            return 0L;
        }
        return ((CountableValueRange<?>) valueRangeDescriptor.extractValueRange(solution, entity)).getSize();
    }

    @Override
    public String toString() {
        return getSimpleEntityAndVariableName() + " variable";
    }

}
