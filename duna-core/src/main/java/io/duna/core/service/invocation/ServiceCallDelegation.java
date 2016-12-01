package io.duna.core.service.invocation;

import co.paralleluniverse.fibers.SuspendExecution;

import java.lang.reflect.Method;

/**
 * Created by carlos on 29/11/16.
 */
public interface ServiceCallDelegation {
    void setMethod(Method target);

    <T> T invoke(Object target, Object ... parameters) throws SuspendExecution;
}
