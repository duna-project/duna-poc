/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation.invocation;

import co.paralleluniverse.fibers.SuspendExecution;

import java.lang.reflect.Method;

public interface ServiceCallDelegation<T> {
    void setMethod(Method target);

    Object invoke(T target, Object ... parameters) throws SuspendExecution;
}
