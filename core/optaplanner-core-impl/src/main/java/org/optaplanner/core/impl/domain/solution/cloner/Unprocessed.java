package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;

class Unprocessed {

    protected Object bean;
    protected Field field;
    protected Object originalValue;

    public Unprocessed(Object bean, Field field, Object originalValue) {
        this.bean = bean;
        this.field = field;
        this.originalValue = originalValue;
    }

}
