package io.duna.core.implementation.invocation;

import java.lang.reflect.Method;

/**
 *
 */
public enum MethodCallDemuxingValidator {
    INSTANCE;

    public boolean isValid(Method method, Object[] arguments) {
        if (method.getParameterCount() != arguments.length) {
            throw new IllegalArgumentException("");
        }

//        for (int i = 0; i < method.getParameterCount(); i++) {
//            if (!(method.getParameterTypes()[i].isAssignableFrom(arguments[i].getClass()))) {
//                throw new IllegalArgumentException("Expected: " + method.getParameterTypes()[i]
//                    + ", Got: " + arguments[i].getClass());
//            }
//        }

        return true;
    }
}
