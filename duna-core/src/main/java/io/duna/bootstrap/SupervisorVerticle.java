/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.bootstrap;

import io.duna.core.context.ClasspathScanner;
import io.duna.core.inject.ExtensionBinderModule;
import io.duna.core.inject.LocalServicesBinderModule;
import io.duna.core.inject.RemoteServicesBinderModule;
import io.duna.core.inject.VerticleBinderModule;
import io.duna.core.service.InterfaceMapper;
import io.duna.core.service.LocalServices;
import io.duna.extend.Port;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.duna.extend.spi.BindingModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import javax.inject.Inject;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
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
        vertx.<Injector>executeBlocking(future -> {
            ClasspathScanner classpathScanner = new ClasspathScanner();

            ObjectMapper defaultObjectMapper = new ObjectMapper(new MessagePackFactory());
            defaultObjectMapper.registerModule(new InterfaceMapper("internal"));

            final ServiceLoader<BindingModule> serviceLoader = ServiceLoader.load(BindingModule.class);

            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Vertx.class)
                        .toInstance(vertx);

                    bind(ObjectMapper.class)
                        .toInstance(defaultObjectMapper);

                    install(new LocalServicesBinderModule(classpathScanner));
                    install(new RemoteServicesBinderModule(classpathScanner));

                    install(new VerticleBinderModule());
                    install(new ExtensionBinderModule(classpathScanner));

                    for (BindingModule module : serviceLoader) {
                        install(module);
                    }
                }
            });

            future.complete(injector);
        }, res -> {
            if (res.failed() || res.result() == null) {
                logger.log(Level.SEVERE, res.cause(), () -> "Error while creating injector");
                return;
            }

            res.result().injectMembers(this);

            verticleFactories.forEach(vertx::registerVerticleFactory);

            vertx.executeBlocking(future -> {
                localServiceNames
                    .stream()
                    .map(n -> "duna:" + n)
                    .forEach(vertx::deployVerticle);

                future.complete();
            }, result -> {
                System.gc();
            });

            vertx.executeBlocking(future -> {
                ports
                    .stream()
                    .map(p -> "duna-port:" + p)
                    .forEach(vertx::deployVerticle);
            }, result -> {
                System.gc();
            });

            startFuture.complete();
        });
    }
}
