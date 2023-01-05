package org.optaplanner.core.impl.solver;

import java.util.IdentityHashMap;
import java.util.Map;

import org.optaplanner.core.config.util.ConfigUtils;

public final class ClassInstanceCache {

    public static ClassInstanceCache create() {
        return new ClassInstanceCache();
    }

    private final Map<Class, Object> singletonMap = new IdentityHashMap<>();

    private ClassInstanceCache() {

    }

    public <T> T newInstance(Object configBean, String propertyName, Class<T> clazz) {
        return (T) singletonMap.computeIfAbsent(clazz, key -> ConfigUtils.newInstance(configBean, propertyName, key));
    }

}
