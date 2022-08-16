package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class ShallowCloningFieldCloner<C> implements FieldCloner<C> {

    @Override
    public Optional<Unprocessed> clone(Field field, Class<? extends C> instaceClass, C original, C clone) {
        Object originalValue = FieldCloner.getFieldValue(original, field);
        FieldCloner.setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }
}
