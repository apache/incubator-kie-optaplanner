package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

/**
 * @implNote This class is not thread-safe.
 */
final class DeepCloningFieldCloner extends AbstractFieldCloner {

    private Class<?> parentType;
    private Class<?> childType;
    private boolean deepCloneDecision;

    public DeepCloningFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(DeepCloningUtils deepCloningUtils, C original, C clone) {
        Object originalValue = AbstractFieldCloner.getGenericFieldValue(original, field);
        if (isDeepCloneField(deepCloningUtils, original.getClass(), originalValue)) { // Defer filling in the field.
            return new Unprocessed(clone, field, originalValue);
        } else { // Shallow copy.
            AbstractFieldCloner.setGenericFieldValue(clone, field, originalValue);
            return null;
        }
    }

    private boolean isDeepCloneField(DeepCloningUtils deepCloningUtils, Class<?> fieldTypeClass, Object originalValue) {
        if (originalValue == null) {
            return false;
        }
        Class<?> originalValueType = originalValue.getClass();
        /*
         * Obtaining the deep clone decision is possibly very expensive, as it involves one or more map lookups.
         * This code sits on the hot path of the lowest level of the solver,
         * and in cases with low acceptedCountLimit (such as simulated annealing) it is called very often,
         * especially on large datasets.
         *
         * This caching mechanism takes advantage of the fact that, for a particular field on a particular class,
         * the types of values contained are unlikely to change and therefore it is safe to cache the calculation.
         * In the unlikely event of a cache miss, we recompute.
         */
        if (parentType != fieldTypeClass || childType != originalValueType) {
            parentType = fieldTypeClass;
            childType = originalValueType;
            deepCloneDecision = deepCloningUtils.getDeepCloneDecision(field, fieldTypeClass, originalValueType);
        }
        return deepCloneDecision;
    }
}
