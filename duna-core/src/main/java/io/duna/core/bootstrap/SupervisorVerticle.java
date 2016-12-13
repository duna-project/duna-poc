/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.duna.core.external.Port;
import io.duna.core.inject.ComponentFactoryBinderModule;
import io.duna.core.inject.LocalServiceBinderModule;
import io.duna.core.inject.RemoteServiceBinderModule;
import io.duna.core.inject.VerticleFactoryBinderModule;
import io.duna.core.service.LocalServices;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.spi.VerticleFactory;

import javax.inject.Inject;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by carlos on 13/12/16.
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class SupervisorVerticle extends AbstractVerticle {

    @Inject
    private Logger logger;

    @Inject
    private Set<VerticleFactory> verticleFactories;

    @Inject @LocalServices
    private Set<String> localServices;

    @Inject @Port
    private Set<String> portExtensions;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toInstance(Vertx.currentContext().owner());
                bind(ObjectMapper.class).toInstance(Json.mapper);

                install(VerticleFactoryBinderModule.INSTANCE);

                install(RemoteServiceBinderModule.INSTANCE);
                install(LocalServiceBinderModule.INSTANCE);

                install(ComponentFactoryBinderModule.INSTANCE);
            }
        });

        injector.injectMembers(this);

        verticleFactories.forEach(vertx::registerVerticleFactory);
        localServices.forEach(vertx::deployVerticle);
        portExtensions.forEach(vertx::deployVerticle);

        startFuture.complete();
    }
}
