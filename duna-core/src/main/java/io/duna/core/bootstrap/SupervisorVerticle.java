/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.bootstrap;

import io.duna.core.classpath.ClassPathScanner;
import io.duna.core.context.ClasspathScanner;
import io.duna.core.inject.LocalServiceBinderModule;
import io.duna.core.inject.component.ExtensionFactoryBinderModule;
import io.duna.core.inject.component.VerticleFactoryBinderModule;
import io.duna.core.inject.service.RemoteServiceBinderModule;
import io.duna.core.service.LocalServices;
import io.duna.port.Port;

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
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public class SupervisorVerticle extends AbstractVerticle {

    @Inject
    private Logger logger;

    @Inject
    private Set<VerticleFactory> verticleFactories;

    @Inject @LocalServices
    private Map<Class<?>, Set<Object>> localServices;

    @Inject @LocalServices
    private Set<String> localServiceNames;

    @Inject @Port
    private Set<String> ports;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.executeBlocking((Future<Injector> f) -> {
            ClasspathScanner classpathScanner = new ClasspathScanner();

            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Vertx.class)
                        .toInstance(vertx);

                    bind(ObjectMapper.class)
                        .toInstance(Json.mapper);

                    install(VerticleFactoryBinderModule.INSTANCE);

                    install(RemoteServiceBinderModule.INSTANCE);
                    install(new LocalServiceBinderModule(classpathScanner));

                    install(ExtensionFactoryBinderModule.INSTANCE);
                }
            });

            f.complete(injector);
        }, res -> {
            if (res.failed()) {
                System.err.println("Error while starting the injector.");
                res.cause().printStackTrace();
                return;
            }

            res.result().injectMembers(this);

            ClassPathScanner.INSTANCE.setScanResult(null);

            verticleFactories.forEach(vertx::registerVerticleFactory);

            vertx.executeBlocking(f -> {
                localServiceNames
                    .stream()
                    .map(n -> "duna:" + n)
                    .forEach(vertx::deployVerticle);

                ports
                    .stream()
                    .map(p -> "duna-port:" + p)
                    .forEach(vertx::deployVerticle);

                f.complete();
            }, r -> {
                System.gc();
                startFuture.complete();
            });
        });
    }
}
