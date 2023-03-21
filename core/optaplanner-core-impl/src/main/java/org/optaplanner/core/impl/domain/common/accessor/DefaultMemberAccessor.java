package org.optaplanner.core.impl.domain.common.accessor;

import java.lang.annotation.Annotation;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.optaplanner.core.impl.domain.common.ReflectionHelper;

final class DefaultMemberAccessor implements MemberAccessor {

    public static <T extends Annotation> MemberAccessor of(Field field, MethodHandles.Lookup lookup) {
        field.setAccessible(true);
        Class<?> declaringClass = field.getDeclaringClass();
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        Type fieldGenericType = field.getGenericType();
        String speedNote = "slow access with reflection";
        Function<Class<T>, T> annotationFunction = field::getAnnotation;
        Function<Class<T>, T[]> annotationsByTypeFunction = field::getDeclaredAnnotationsByType;
        String toString = "field " + field;
        try {
            MethodHandle getter = unreflectGetter(field, lookup);
            MethodHandle setter = unreflectSetter(field, lookup);
            return new DefaultMemberAccessor(declaringClass, fieldName, fieldType, fieldGenericType, speedNote,
                    annotationFunction, annotationsByTypeFunction, toString, x -> {
                        try {
                            return getter.invoke(x);
                        } catch (Throwable e) {
                            throw new IllegalStateException(e);
                        }
                    }, (x, y) -> {
                        try {
                            setter.invoke(x, y);
                        } catch (Throwable e) {
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (Throwable e) {
            throw new IllegalStateException("Lambda creation failed for (" + toString + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    private static String getSpeedNote(Method method) {
        return isLambdaSupported(method) ? "pretty fast access with LambdaMetafactory" : "slow access with reflection";
    }

    private static MethodHandle unreflectGetter(Field field, MethodHandles.Lookup lookup) {
        try {
            return lookup.unreflectGetter(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed unreflecting getter for field (" + field + ").", e);
        }
    }

    private static MethodHandle unreflectSetter(Field field, MethodHandles.Lookup lookup) {
        try {
            return lookup.unreflectSetter(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed unreflecting setter for field (" + field + ").", e);
        }
    }

    public static <T extends Annotation> MemberAccessor of(Method method, MethodHandles.Lookup lookup) {
        method.setAccessible(true);
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        String speedNote = getSpeedNote(method);
        Function<Class<T>, T> annotationFunction = method::getAnnotation;
        Function<Class<T>, T[]> annotationsByTypeFunction = method::getDeclaredAnnotationsByType;
        String toString = "method " + methodName + " on " + declaringClass;
        Function getter = unreflectGetterMethod(method, lookup);
        return new DefaultMemberAccessor(declaringClass, methodName, returnType, genericReturnType, speedNote,
                annotationFunction, annotationsByTypeFunction, toString, getter, null);
    }

    private static Function unreflectGetterMethod(Method getterMethod, MethodHandles.Lookup lookup) {
        try {
            MethodHandle unreflected = lookup.unreflect(getterMethod);
            if (isLambdaSupported(getterMethod)) {
                return wrapGetter(unreflected, getterMethod.getReturnType(), getterMethod.getDeclaringClass(), lookup);
            } else {
                return x -> {
                    try {
                        return unreflected.invoke(x);
                    } catch (Throwable e) {
                        throw new IllegalStateException(e);
                    }
                };
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Lambda creation failed for getterMethod (" + getterMethod + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    private static boolean isLambdaSupported(Method method) {
        // HACK The lambda approach doesn't support classes from another classloader (such as loaded by KieContainer) in JDK 8
        return Modifier.isPublic(method.getModifiers())
                && method.getDeclaringClass().getClassLoader().equals(MemberAccessor.class.getClassLoader());
    }

    private static Function wrapGetter(MethodHandle methodHandle, Class<?> returnType, Class<?> declaringClass,
            MethodHandles.Lookup lookup) throws Throwable {
        return (Function) LambdaMetafactory.metafactory(lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                methodHandle,
                MethodType.methodType(returnType, declaringClass))
                .getTarget().invokeExact();
    }

    private static BiConsumer unreflectSetterMethod(Method setterMethod, MethodHandles.Lookup lookup) {
        try {
            MethodHandle unreflected = lookup.unreflect(setterMethod);
            if (isLambdaSupported(setterMethod)) {
                return wrapSetter(unreflected, setterMethod.getParameterTypes()[0], setterMethod.getDeclaringClass(), lookup);
            } else {
                return (x, y) -> {
                    try {
                        unreflected.invoke(x, y);
                    } catch (Throwable e) {
                        throw new IllegalStateException(e);
                    }
                };
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Lambda creation failed for setterMethod (" + setterMethod + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    private static BiConsumer wrapSetter(MethodHandle methodHandle, Class<?> propertyType, Class<?> declaringClass,
            MethodHandles.Lookup lookup) throws Throwable {
        return (BiConsumer) LambdaMetafactory.metafactory(lookup,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class),
                methodHandle,
                MethodType.methodType(void.class, declaringClass, propertyType))
                .getTarget()
                .invoke();
    }

    public static <T extends Annotation> MemberAccessor of(Method method, boolean getterOnly, MethodHandles.Lookup lookup) {
        method.setAccessible(true);
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = ReflectionHelper.getGetterPropertyName(method);
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        String speedNote = getSpeedNote(method);
        Function<Class<T>, T> annotationFunction = method::getAnnotation;
        Function<Class<T>, T[]> annotationsByTypeFunction = method::getDeclaredAnnotationsByType;
        String toString = "bean property " + methodName + " on " + method.getDeclaringClass();
        try {
            Function getter = unreflectGetterMethod(method, lookup);
            if (getterOnly) {
                return new DefaultMemberAccessor(declaringClass, methodName, returnType, genericReturnType,
                        speedNote, annotationFunction, annotationsByTypeFunction, toString, getter, null);
            } else {
                Method setterMethod = ReflectionHelper.getSetterMethod(declaringClass, returnType, methodName);
                if (setterMethod != null) {
                    setterMethod.setAccessible(true);
                    BiConsumer setter = unreflectSetterMethod(setterMethod, lookup);
                    return new DefaultMemberAccessor(declaringClass, methodName, returnType, genericReturnType,
                            speedNote, annotationFunction, annotationsByTypeFunction, toString, getter, setter);
                } else {
                    return new DefaultMemberAccessor(declaringClass, methodName, returnType, genericReturnType,
                            speedNote, annotationFunction, annotationsByTypeFunction, toString, getter, null);
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Lambda creation failed for (" + toString + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    private final Class<?> declaringClass;
    private final String name;
    private final Class<?> type;
    private final Type genericType;
    private final String speedNote;
    private final Function annotationFunction;
    private final Function annotationsByTypeFunction;
    private final String toString;
    private final Function getter;
    private final BiConsumer setter;

    private <T extends Annotation> DefaultMemberAccessor(Class<?> declaringClass, String name, Class<?> type, Type genericType,
            String speedNote, Function<Class<T>, T> annotationFunction, Function<Class<T>, T[]> annotationsByTypeFunction,
            String toString, Function getter, BiConsumer setter) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.type = type;
        this.genericType = genericType;
        this.speedNote = speedNote;
        this.annotationFunction = annotationFunction;
        this.annotationsByTypeFunction = annotationsByTypeFunction;
        this.toString = toString;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public Object executeGetter(Object bean) {
        try {
            return getter.apply(bean);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot get (" + this + ") on bean of class (" + bean.getClass() + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    @Override
    public boolean supportSetter() {
        return setter != null;
    }

    @Override
    public void executeSetter(Object bean, Object value) {
        if (!supportSetter()) {
            throw new UnsupportedOperationException();
        }
        try {
            setter.accept(bean, value);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot set (" + this + ") on bean of class (" + bean.getClass() + ").\n" +
                    MemberAccessorFactory.CLASSLOADER_NUDGE_MESSAGE, e);
        }
    }

    @Override
    public String getSpeedNote() {
        return speedNote;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) annotationFunction.apply(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return (T[]) annotationsByTypeFunction.apply(annotationClass);
    }

    @Override
    public <Fact_, Result_> Function<Fact_, Result_> getGetterFunction() {
        return getter;
    }

    @Override
    public String toString() {
        return toString;
    }
}
