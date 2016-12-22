/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.bootstrap;

import io.duna.core.context.ClasspathScanner;
import io.duna.core.inject.ExtensionBinderModule;
import io.duna.core.inject.LocalServicesBinderModule;
import io.duna.core.inject.RemoteServicesBinderModule;
import io.duna.core.inject.VerticleBinderModule;
import io.duna.core.service.LocalServices;
import io.duna.extend.Port;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.spi.VerticleFactory;

import javax.inject.Inject;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public class SupervisorVerticle extends AbstractVerticle {

    private Logger logger = LogManager.getLogManager()
        .getLogger(SupervisorVerticle.class.getName());

    @Inject
    private Set<VerticleFactory> verticleFactories;

    @Inject @LocalServices
    private Set<String> localServiceNames;

    @Inject @Port
    private Set<String> ports;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        ClasspathScanner classpathScanner = new ClasspathScanner();

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class)
                    .toInstance(vertx);

                bind(ObjectMapper.class)
                    .toInstance(Json.mapper);

                install(new LocalServicesBinderModule(classpathScanner));
                install(new RemoteServicesBinderModule(classpathScanner));

                install(new VerticleBinderModule());
                install(new ExtensionBinderModule(classpathScanner));
            }
        });


        injector.injectMembers(this);

        verticleFactories.forEach(vertx::registerVerticleFactory);

        localServiceNames
            .stream()
            .map(n -> "duna:" + n)
            .forEach(vertx::deployVerticle);

        ports
            .stream()
            .map(p -> "duna-port:" + p)
            .forEach(vertx::deployVerticle);

        System.gc();
        startFuture.complete();
    }
}
