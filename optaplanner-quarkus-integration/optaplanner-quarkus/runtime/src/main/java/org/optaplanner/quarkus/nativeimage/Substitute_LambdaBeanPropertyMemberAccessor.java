package org.optaplanner.quarkus.nativeimage;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * LambdaBeanPropertyMemberAccessor works by creating a new class during runtime (via LambdaMetafactory) to delegate to
 * the provided getter/setter methods. This is not supported in GraalVM, so we need to use Method reflection
 * (i.e. {@link Method#invoke(Object, Object...)}) instead.
 */
@TargetClass(className = "org.optaplanner.core.impl.domain.common.accessor.LambdaBeanPropertyMemberAccessor")
public final class Substitute_LambdaBeanPropertyMemberAccessor {

    @Alias
    Method getterMethod;

    @Alias
    Method setterMethod;

    @Substitute
    private Function createGetterFunction(MethodHandles.Lookup lookup) {
        return new GetterFunctionDelegator(getterMethod);
    }

    @Substitute
    private BiConsumer createSetterFunction(MethodHandles.Lookup lookup) {
        if (setterMethod == null) {
            return null;
        }

        return new SetterFunctionDelegator(setterMethod);
    }

    private static final class GetterFunctionDelegator implements Function {
        private final Method method;

        public GetterFunctionDelegator(Method method) {
            this.method = method;
        }

        @Override
        public Object apply(Object object) {
            try {
                return method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class SetterFunctionDelegator implements BiConsumer {
        private final Method method;

        public SetterFunctionDelegator(Method method) {
            this.method = method;
        }

        @Override
        public void accept(Object object, Object value) {
            try {
                method.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
