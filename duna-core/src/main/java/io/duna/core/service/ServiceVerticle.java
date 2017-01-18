/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service;

import io.duna.core.service.handler.DefaultServiceHandler;
import io.duna.core.util.Services;

import co.paralleluniverse.fibers.Suspendable;
import com.google.inject.assistedinject.Assisted;
import io.vertx.ext.sync.SyncVerticle;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
        final Class<? extends Annotation> qualifier = Services
            .getQualifier(serviceInstance.getClass());

        final String qualifierPrefix;

        if (qualifier != null) qualifierPrefix = qualifier.getName() + "@";
        else qualifierPrefix = "";

        logger.info(() -> "Registering verticle for service " + contractClass.getName());

        vertx.executeBlocking(f -> {
            for (Method method : contractClass.getMethods()) {
                final String serviceAddress = qualifierPrefix
                    + Services.getInternalServiceAddress(method);

                logger.fine(() -> "Registering consumer at address " + serviceAddress);
                vertx.eventBus().consumer(serviceAddress,
                    fiberHandler(handlerFactory.create(serviceInstance, method)));
                logger.finer(() -> "Consumer registered");
            }

            f.complete();
        }, res -> {
            logger.finer(() -> "Verticle registered");
        });
    }

    public interface BinderFactory {
        ServiceVerticle create(Class<?> contractClass, Object serviceInstance);
    }
}
