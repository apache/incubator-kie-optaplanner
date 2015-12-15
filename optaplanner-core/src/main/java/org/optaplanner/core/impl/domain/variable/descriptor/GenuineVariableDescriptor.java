/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.lang3.ArrayUtils;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
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
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class GenuineVariableDescriptor extends VariableDescriptor {

    private boolean chained;

    private ValueRangeDescriptor valueRangeDescriptor;
    private boolean nullable;
    private SelectionFilter reinitializeVariableEntityFilter;
    private SelectionSorter increasingStrengthSorter;
    private SelectionSorter decreasingStrengthSorter;

    public GenuineVariableDescriptor(EntityDescriptor entityDescriptor,
            MemberAccessor variableMemberAccessor) {
        super(entityDescriptor, variableMemberAccessor);
    }

    public void processAnnotations(DescriptorPolicy descriptorPolicy) {
        processPropertyAnnotations(descriptorPolicy);
    }

    private void processPropertyAnnotations(DescriptorPolicy descriptorPolicy) {
        PlanningVariable planningVariableAnnotation = variableMemberAccessor.getAnnotation(PlanningVariable.class);
        processNullable(descriptorPolicy, planningVariableAnnotation);
        processChained(descriptorPolicy, planningVariableAnnotation);
        processValueRangeRefs(descriptorPolicy, planningVariableAnnotation);
        processStrength(descriptorPolicy, planningVariableAnnotation);
    }

    private void processNullable(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        nullable = planningVariableAnnotation.nullable();
        if (nullable && variableMemberAccessor.getType().isPrimitive()) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variableMemberAccessor.getName()
                    + ") with nullable (" + nullable + "), which is not compatible with the primitive propertyType ("
                    + variableMemberAccessor.getType() + ").");
        }
        Class<? extends SelectionFilter> reinitializeVariableEntityFilterClass
                = planningVariableAnnotation.reinitializeVariableEntityFilter();
        if (reinitializeVariableEntityFilterClass == PlanningVariable.NullReinitializeVariableEntityFilter.class) {
            reinitializeVariableEntityFilterClass = null;
        }
        if (reinitializeVariableEntityFilterClass != null) {
            reinitializeVariableEntityFilter = ConfigUtils.newInstance(this,
                    "reinitializeVariableEntityFilterClass", reinitializeVariableEntityFilterClass);
        } else {
            reinitializeVariableEntityFilter = new NullValueReinitializeVariableEntityFilter(this);
        }
    }

    private void processChained(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        chained = planningVariableAnnotation.graphType() == PlanningVariableGraphType.CHAINED;
        if (chained && !variableMemberAccessor.getType().isAssignableFrom(
                entityDescriptor.getEntityClass())) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variableMemberAccessor.getName()
                    + ") with chained (" + chained + ") and propertyType (" + variableMemberAccessor.getType()
                    + ") which is not a superclass/interface of or the same as the entityClass ("
                    + entityDescriptor.getEntityClass() + ").");
        }
        if (chained && nullable) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variableMemberAccessor.getName()
                    + ") with chained (" + chained + "), which is not compatible with nullable (" + nullable + ").");
        }
    }

    private void processValueRangeRefs(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        String[] valueRangeProviderRefs = planningVariableAnnotation.valueRangeProviderRefs();
        if (ArrayUtils.isEmpty(valueRangeProviderRefs)) {
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a " + PlanningVariable.class.getSimpleName()
                    + " annotated property (" + variableMemberAccessor.getName()
                    + ") that has no valueRangeProviderRefs (" + Arrays.toString(valueRangeProviderRefs) + ").");
        }
        List<ValueRangeDescriptor> valueRangeDescriptorList = new ArrayList<ValueRangeDescriptor>(valueRangeProviderRefs.length);
        boolean addNullInValueRange = nullable && valueRangeProviderRefs.length == 1;
        for (String valueRangeProviderRef : valueRangeProviderRefs) {
            valueRangeDescriptorList.add(buildValueRangeDescriptor(descriptorPolicy, valueRangeProviderRef, addNullInValueRange));
        }
        if (valueRangeDescriptorList.size() == 1) {
            valueRangeDescriptor = valueRangeDescriptorList.get(0);
        } else {
            valueRangeDescriptor = new CompositeValueRangeDescriptor(this, nullable, valueRangeDescriptorList);
        }
    }

    private ValueRangeDescriptor buildValueRangeDescriptor(DescriptorPolicy descriptorPolicy,
            String valueRangeProviderRef, boolean addNullInValueRange) {
        if (descriptorPolicy.hasFromSolutionValueRangeProvider(valueRangeProviderRef)) {
            MemberAccessor memberAccessor = descriptorPolicy.getFromSolutionValueRangeProvider(valueRangeProviderRef);
            return new FromSolutionPropertyValueRangeDescriptor(this, addNullInValueRange, memberAccessor);
        } else if (descriptorPolicy.hasFromEntityValueRangeProvider(valueRangeProviderRef)) {
            MemberAccessor memberAccessor = descriptorPolicy.getFromEntityValueRangeProvider(valueRangeProviderRef);
            return new FromEntityPropertyValueRangeDescriptor(this, addNullInValueRange, memberAccessor);
        } else {
            Collection<String> providerIds = descriptorPolicy.getValueRangeProviderIds();
            throw new IllegalArgumentException("The entityClass (" + entityDescriptor.getEntityClass()
                    + ") has a @" + PlanningVariable.class.getSimpleName()
                    + " property (" + variableMemberAccessor.getName()
                    + ") with a valueRangeProviderRef (" + valueRangeProviderRef
                    + ") that does not exist in a @" + ValueRangeProvider.class.getSimpleName()
                    + " on the solution class ("
                    + entityDescriptor.getSolutionDescriptor().getSolutionClass().getSimpleName()
                    + ") or on that entityClass.\n"
                    + "The valueRangeProviderRef (" + valueRangeProviderRef
                    + ") does not appear in the valueRangeProvideIds (" + providerIds
                    + ")." + (!providerIds.isEmpty() ? "" : "\nMaybe a @" + ValueRangeProvider.class.getSimpleName()
                    + " annotation is missing on a method in the solution class ("
                    + entityDescriptor.getSolutionDescriptor().getSolutionClass().getSimpleName() + ")."));
        }
    }

    private void processStrength(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        Class<? extends Comparator> strengthComparatorClass = planningVariableAnnotation.strengthComparatorClass();
        if (strengthComparatorClass == PlanningVariable.NullStrengthComparator.class) {
            strengthComparatorClass = null;
        }
        Class<? extends SelectionSorterWeightFactory> strengthWeightFactoryClass
                = planningVariableAnnotation.strengthWeightFactoryClass();
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
            Comparator<Object> strengthComparator = ConfigUtils.newInstance(this,
                    "strengthComparatorClass", strengthComparatorClass);
            increasingStrengthSorter = new ComparatorSelectionSorter(
                    strengthComparator, SelectionSorterOrder.ASCENDING);
            decreasingStrengthSorter = new ComparatorSelectionSorter(
                    strengthComparator, SelectionSorterOrder.DESCENDING);
        }
        if (strengthWeightFactoryClass != null) {
            SelectionSorterWeightFactory strengthWeightFactory = ConfigUtils.newInstance(this,
                    "strengthWeightFactoryClass", strengthWeightFactoryClass);
            increasingStrengthSorter = new WeightFactorySelectionSorter(
                    strengthWeightFactory, SelectionSorterOrder.ASCENDING);
            decreasingStrengthSorter = new WeightFactorySelectionSorter(
                    strengthWeightFactory, SelectionSorterOrder.DESCENDING);
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isChained() {
        return chained;
    }

    public boolean isNullable() {
        return nullable;
    }

    public SelectionFilter getReinitializeVariableEntityFilter() {
        return reinitializeVariableEntityFilter;
    }

    public ValueRangeDescriptor getValueRangeDescriptor() {
        return valueRangeDescriptor;
    }

    public boolean isValueRangeEntityIndependent() {
        return valueRangeDescriptor.isEntityIndependent();
    }

    // ************************************************************************
    // Extraction methods
    // ************************************************************************

    /**
     * A {@link PlanningVariable#nullable()} value is always considered initialized, but it can still be reinitialized
     * with {@link PlanningVariable#reinitializeVariableEntityFilter()}.
     * @param entity never null
     * @return true if the variable on that entity is initialized
     */
    public boolean isInitialized(Object entity) {
        if (nullable) {
            return true;
        }
        Object variable = getValue(entity);
        return variable != null;
    }

    public boolean isReinitializable(ScoreDirector scoreDirector, Object entity) {
        return reinitializeVariableEntityFilter.accept(scoreDirector, entity);
    }

    public SelectionSorter getIncreasingStrengthSorter() {
        return increasingStrengthSorter;
    }

    public SelectionSorter getDecreasingStrengthSorter() {
        return decreasingStrengthSorter;
    }

    public long getValueCount(Solution solution, Object entity) {
        if (!valueRangeDescriptor.isCountable()) {
            // TODO report this better than just ignoring it
            return 0L;
        }
        return ((CountableValueRange<?>) valueRangeDescriptor.extractValueRange(solution, entity)).getSize();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + variableMemberAccessor.getName()
                + " of " + entityDescriptor.getEntityClass().getName() + ")";
    }

}
