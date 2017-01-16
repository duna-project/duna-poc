/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.service;

import io.duna.core.context.ClasspathScanner;
import io.duna.core.service.InterfaceMapper;
import io.duna.core.util.Services;
import io.duna.extend.Port;
import io.duna.http.HttpInterface;
import io.duna.http.HttpInterfaces;
import io.duna.http.HttpPath;
import io.duna.http.service.handler.HttpJsonServiceHandler;
import io.duna.http.util.Paths;
import io.duna.serialization.Json;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.vertx.ext.sync.Sync.fiberHandler;

@Port
public class HttpRouterVerticle extends SyncVerticle {

    private final Logger logger;

    private final Injector injector;

    private final Config config;

    @Inject
    public HttpRouterVerticle(Logger logger,
                              Injector injector) {
        this.logger = logger;
        this.injector = injector;
        this.config = ConfigFactory.load();
    }

    @Suspendable
    @Override
    public void start() throws Exception {
        logger.info(() -> "Starting the HTTP port");

        vertx.executeBlocking((Future<Router> future) -> {
            logger.fine(() -> "Creating child injector with web handler factory");

            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.registerModule(new InterfaceMapper("external"));

            Injector handlerInjector = injector
                .createChildInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ObjectMapper.class)
                            .annotatedWith(Json.class)
                            .toInstance(jsonObjectMapper);

                        install(new FactoryModuleBuilder()
                            .implement(HttpJsonServiceHandler.class, HttpJsonServiceHandler.class)
                            .build(HttpJsonServiceHandler.BinderFactory.class));
                    }
                });

            HttpJsonServiceHandler.BinderFactory handlerFactory =
                handlerInjector.getInstance(HttpJsonServiceHandler.BinderFactory.class);

            logger.fine(() -> "Starting the HTTP router");

            Router router = Router.router(vertx);
            router
                .route()
                .handler(BodyHandler.create());

            logger.info(() -> "Registering HTTP interfaces");

            ClasspathScanner classpathScanner = new ClasspathScanner();

            classpathScanner
                .getAllContracts()
                .stream()
                .map(contractName -> {
                    try {
                        return Class.forName(contractName);
                    } catch (ClassNotFoundException ex) {
                        logger.warning(() -> "Class " + contractName + " not found in the classpath");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(contract -> registerRouteToService(router, handlerFactory, contract));

            future.complete(router);
        }, fiberHandler((result) -> {
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
                    .listen(serverPort, serverHost,
                        h -> logger.info("HTTP server started at port " + serverPort));

                f.complete();
            }, res -> {
                System.gc();
            });
        }));
    }

    private void registerRouteToService(Router router,
                                        HttpJsonServiceHandler.BinderFactory handlerFactory,
                                        Class<?> contractClass) {

        if (!Paths.isExposed(contractClass)) {
            throw new IllegalArgumentException(contractClass.getName() + " must be exposed by an @HttpPath.");
        }

        final String servicePath;

        if (contractClass.isAnnotationPresent(HttpPath.class)) {
            servicePath = contractClass.getAnnotation(HttpPath.class).value();
        } else {
            servicePath = "/" + contractClass.getName();
        }

        Set<Method> exposedMethods = Arrays
            .stream(contractClass.getMethods())
            .filter(m -> m.isAnnotationPresent(HttpInterface.class)
                || m.isAnnotationPresent(HttpInterfaces.class))
            .collect(Collectors.toSet());

        logger.fine(() -> "Registering HTTP routes for " + contractClass.getName());

        // Register HTTP interfaces
        exposedMethods.forEach(
            exposedMethod -> Arrays.stream(exposedMethod.getAnnotationsByType(HttpInterface.class))
                .forEach(annotation -> {
                    String methodPath;

                    if (!annotation.path().startsWith("/")) {
                        methodPath = "/" + annotation.path();
                    } else {
                        methodPath = annotation.path();
                    }

                    // Routing with regex currently not supported
                    HttpMethod method = HttpMethod.valueOf(annotation.method().name());
                    Route route = router.route(method, servicePath + methodPath);

//                    if (annotation.method() != io.duna.http.HttpMethod.GET) route.consumes("*/json");

                    route.handler(fiberHandler(handlerFactory.create(contractClass, exposedMethod, annotation.path())));

                    logger.fine(() -> "Registered " + method + " route " + annotation.path()
                        + " to " + Services.getInternalServiceAddress(exposedMethod));
                }));
    }
}
