/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http;

import io.duna.core.service.LocalServices;
import io.duna.http.service.handler.HttpServiceHandler;
import io.duna.port.Port;

import co.paralleluniverse.fibers.Suspendable;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Future;
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

import static io.vertx.ext.sync.Sync.fiberHandler;

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
                .createChildInjector(new FactoryModuleBuilder()
                    .implement(HttpServiceHandler.class, HttpServiceHandler.class)
                    .build(HttpServiceHandler.BinderFactory.class));

            HttpServiceHandler.BinderFactory handlerFactory =
                handlerInjector.getBinding(HttpServiceHandler.BinderFactory.class).getProvider().get();

            logger.fine(() -> "Starting the HTTP router");

            Router router = Router.router(vertx);

            logger.fine(() -> "Registering HTTP interfaces");
            for (Map.Entry<Class<?>, Object> serviceEntry : localServices.entrySet()) {
                if (!isExposed(serviceEntry.getValue())) continue;

                Set<Method> exposedMethods = Arrays
                    .stream(serviceEntry.getValue().getClass().getMethods())
                    .filter(m -> m.isAnnotationPresent(HttpMethod.class))
                    .collect(Collectors.toSet());

                logger.finer(() -> "Registering HTTP routes for " + serviceEntry.getKey().getName());
                exposedMethods.forEach(m -> {
                    StringBuilder address = new StringBuilder("/");

                    Path path = m.getAnnotation(Path.class);
                    address.append(path != null ? path.value() : m.getName());

                    logger.info(() -> "Registering route " + address + " to " + m.toString());

                    Arrays.stream(m.getAnnotation(HttpMethod.class).value())
                        .map(v -> io.vertx.core.http.HttpMethod.valueOf(v.name()))
                        .forEach(httpMethod -> router
                            .route(httpMethod, address.toString())
                            .handler(fiberHandler(handlerFactory
                            .create(serviceEntry.getValue(), m))));

                    logger.fine(() -> "Route registered");
                });
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
            }, res -> {});
        }));
    }

    private boolean isExposed(Object service) {
        return service.getClass().isAnnotationPresent(HttpPort.class);
    }
}
