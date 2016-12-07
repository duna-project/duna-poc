package io.duna.core.implementation.invocation;

import co.paralleluniverse.fibers.SuspendExecution;

import java.lang.reflect.Method;

public interface ServiceCallDelegation<T> {
    void setMethod(Method target);

    Object invoke(T target, Object ... parameters) throws SuspendExecution;
}
