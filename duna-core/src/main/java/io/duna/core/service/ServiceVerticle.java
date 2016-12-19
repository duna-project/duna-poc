/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service;

import io.duna.core.service.handler.DefaultServiceHandler;
import io.duna.util.Services;

import co.paralleluniverse.fibers.Suspendable;
import com.google.inject.assistedinject.Assisted;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import static io.vertx.ext.sync.Sync.fiberHandler;

public class ServiceVerticle extends SyncVerticle {

    private final Class<?> contractClass;

    private final Object serviceInstance;

    private final Logger logger;

    private final DefaultServiceHandler.BinderFactory handlerFactory;

    @Inject
    public ServiceVerticle(@Assisted Class<?> contractClass,
                           @Assisted Object serviceInstance,
                           Logger logger,
                           DefaultServiceHandler.BinderFactory handlerFactory) {
        this.contractClass = contractClass;
        this.serviceInstance = serviceInstance;
        this.logger = logger;
        this.handlerFactory = handlerFactory;
    }

    @Suspendable
    @Override
    public void start() throws Exception {
        final Annotation qualifier = Services.INSTANCE
            .getQualifier(serviceInstance.getClass());

        final String qualifierPrefix;

        if (qualifier != null) qualifierPrefix = qualifier.getClass().getSimpleName() + "@";
        else qualifierPrefix = "";

        logger.info(() -> "Registering verticle for service " + contractClass.getName());

        for (Method method : contractClass.getMethods()) {
            final String serviceAddress = qualifierPrefix +
                Services.INSTANCE.getInternalServiceAddress(method, ".");

            vertx.executeBlocking(
                (Future<Handler<Message<Buffer>>> f) ->
                    f.complete(fiberHandler(handlerFactory.create(serviceInstance, method))),
                result -> {
                    logger.fine(() -> "Registering consumer at address " + serviceAddress);
                    vertx.eventBus().consumer(serviceAddress, result.result());
                });
        }

        System.gc();
    }

    public interface BinderFactory {
        ServiceVerticle create(Class<?> contractClass, Object serviceInstance);
    }
}
