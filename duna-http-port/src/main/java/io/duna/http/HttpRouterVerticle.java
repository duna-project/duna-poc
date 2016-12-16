/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.duna.core.service.LocalServices;
import io.duna.http.service.handler.HttpServiceHandler;
import io.duna.port.Port;
import io.duna.serialization.Json;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.fiberHandler;
import static java.util.stream.Stream.concat;

@Port
public class HttpRouterVerticle extends SyncVerticle {

    private final Map<Class<?>, Object> localServices;

    private final Logger logger;

    private final Injector injector;

    private final Config config;

    @Inject
    public HttpRouterVerticle(@LocalServices Map<Class<?>, Object> localServices,
                              Logger logger,
                              Injector injector) {
        this.localServices = localServices;
        this.logger = logger;
        this.injector = injector;
        this.config = ConfigFactory.load();
    }

    @Suspendable
    @Override
    public void start() throws Exception {
        logger.info(() -> "Starting the HTTP port");

        vertx.executeBlocking(fiberHandler((Future<Router> future) -> {
            logger.fine(() -> "Creating child injector with web handler factory");

            Injector handlerInjector = injector
                .createChildInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ObjectMapper.class)
                            .annotatedWith(Json.class)
                            .toInstance(io.vertx.core.json.Json.prettyMapper);

                        install(new FactoryModuleBuilder()
                            .implement(HttpServiceHandler.class, HttpServiceHandler.class)
                            .build(HttpServiceHandler.BinderFactory.class));
                    }
                });

            HttpServiceHandler.BinderFactory handlerFactory =
                handlerInjector.getBinding(HttpServiceHandler.BinderFactory.class).getProvider().get();

            logger.fine(() -> "Starting the HTTP router");

            Router router = Router.router(vertx);

            logger.fine(() -> "Registering HTTP interfaces");
            for (Map.Entry<Class<?>, Object> serviceEntry : localServices.entrySet()) {
                if (!isExposed(serviceEntry.getKey())) continue;

                Set<Method> exposedMethods = Arrays
                    .stream(serviceEntry.getKey().getMethods())
                    .filter(m -> m.isAnnotationPresent(HttpInterface.class) ||
                        m.isAnnotationPresent(HttpInterfaces.class))
                    .collect(Collectors.toSet());

                logger.finer(() -> "Registering HTTP routes for " + serviceEntry.getKey().getName());

                exposedMethods.forEach(m -> // concat(
                    // Arrays.stream(m.getAnnotation(HttpInterfaces.class).value()),
                    Arrays.stream(m.getAnnotationsByType(HttpInterface.class)) //)
                    .forEach(annotation -> {
                        try {
                            router
                                .route(HttpMethod.valueOf(annotation.method().name()), annotation.path())
                                .handler(fiberHandler(handlerFactory
                                    .create(serviceEntry.getValue(), m)));

                            logger.finer(() -> "Registered route " + annotation.path() + " to " + m.toString());
                        } catch (IllegalArgumentException e) {
                            logger.severe(() -> "Can't register route '" +
                                annotation.path() + "' to '" + m.toString() +
                                "'. The path must start with /.");
                        }
                    }));
            }

            future.complete(router);
        }), fiberHandler((result) -> {
            // Route registration complete
            if (result.failed()) {
                logger.log(Level.SEVERE, "Error while starting HTTP port", result.cause());
                return;
            }

            vertx.executeBlocking(f -> {
                String serverHost = config.getString("duna.web.host");
                int serverPort = config.getInt("duna.web.port");

                logger.fine(() -> "Starting HTTP server at port " + serverPort);

                vertx.createHttpServer()
                    .requestHandler(result.result()::accept)
                    .listen(serverPort, serverHost, h -> logger.info("HTTP server started at port " + serverPort));

                f.complete();
            }, res -> {
            });
        }));
    }

    private boolean isExposed(Class<?> contract) {
        return contract.isAnnotationPresent(HttpPort.class);
    }
}
