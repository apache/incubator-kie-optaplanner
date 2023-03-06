package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

final class ShallowCloningFieldCloner extends AbstractFieldCloner {

    public ShallowCloningFieldCloner(Field field) {
        super(field);
    }

    @Override
    public <C> Unprocessed clone(C original, C clone) {
        Object originalValue = AbstractFieldCloner.getGenericFieldValue(original, field);
        AbstractFieldCloner.setGenericFieldValue(clone, field, originalValue);
        return null;
    }

}
