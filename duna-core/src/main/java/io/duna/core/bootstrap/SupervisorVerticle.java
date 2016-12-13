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
import io.duna.core.classpath.ClassPathScanner;
import io.duna.core.inject.component.ExtensionFactoryBinderModule;
import io.duna.core.inject.service.LocalServiceBinderModule;
import io.duna.core.inject.service.RemoteServiceBinderModule;
import io.duna.core.inject.component.VerticleFactoryBinderModule;
import io.duna.core.service.LocalServices;
import io.duna.port.Port;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.spi.VerticleFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by carlos on 13/12/16.
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "SpringAutowiredFieldsWarningInspection"})
public class SupervisorVerticle extends AbstractVerticle {

    @Inject
    private Logger logger;

    @Inject
    private Set<VerticleFactory> verticleFactories;

    @Inject @LocalServices
    private Map<Class<?>, Object> localServices;

    @Inject @Port
    private Set<String> ports;

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

                install(ExtensionFactoryBinderModule.INSTANCE);
            }
        });

        injector.injectMembers(this);

        verticleFactories.forEach(vertx::registerVerticleFactory);

        vertx.executeBlocking(f -> {
            localServices
                .keySet()
                .parallelStream()
                .map(c -> "duna:" + c.getName())
                .forEach(vertx::deployVerticle);

            System.out.println(ports);
            System.out.println(ClassPathScanner.INSTANCE.getPortExtensions());

            ports
                .stream()
                .map(p -> "Registering port " + p)
                .forEach(logger::info);

            ports
                .parallelStream()
                .map(p -> "duna-port:" + p)
                .forEach(vertx::deployVerticle);

            f.complete();
        }, r -> {
            startFuture.complete();
        });
    }
}
