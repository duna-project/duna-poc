package io.duna.web;

import co.paralleluniverse.fibers.Suspendable;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.duna.core.service.LocalServices;
import io.duna.port.Port;
import io.duna.util.Services;
import io.duna.web.annotations.HttpInterface;
import io.duna.web.annotations.HttpMethod;
import io.duna.web.service.handler.RestServiceHandler;
import io.vertx.core.Future;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.vertx.ext.sync.Sync.*;

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
                    .implement(RestServiceHandler.class, RestServiceHandler.class)
                    .build(RestServiceHandler.BinderFactory.class));

            RestServiceHandler.BinderFactory handlerFactory =
                handlerInjector.getBinding(RestServiceHandler.BinderFactory.class).getProvider().get();

            logger.fine(() -> "Starting the HTTP router");

            Router router = Router.router(vertx);

            Set<Class<?>> exposedServices = localServices.keySet()
                .parallelStream()
                .filter(c -> c.isAnnotationPresent(HttpInterface.class))
                .collect(Collectors.toSet());

            logger.fine(() -> "Registering HTTP interfaces");
            for (Class<?> contract : exposedServices) {

                logger.finer(() -> "Registering entry points for " + contract.getName());
                for (Method method: contract.getMethods()) {
                    HttpMethod annotation = method.getAnnotation(HttpMethod.class);
                    io.vertx.core.http.HttpMethod httpMethod;

                    if (annotation == null) httpMethod = io.vertx.core.http.HttpMethod.GET;
                    else httpMethod = io.vertx.core.http.HttpMethod.valueOf(annotation.value().name());

                    String address = "/" + Services.INSTANCE.getUniqueServiceAddress(method, "/");

                    logger.info("Registering route " + address + " -> " + method.toString());

                    router.route(httpMethod, address).handler(
                        fiberHandler(handlerFactory
                            .create(localServices.get(contract), method)));
                }
            }

            future.complete(router);

        }), fiberHandler((result) -> {
            // Computation complete

            if (result.failed()) {
                logger.log(Level.SEVERE, "Error while starting HTTP port", result.cause());
                return;
            }

            String serverHost = config.getString("duna.web.host");
            int serverPort = config.getInt("duna.web.port");

            vertx.createHttpServer()
                .requestHandler(result.result()::accept)
                .listen(serverPort, serverHost, h -> logger.info("HTTP server started at port " + serverPort));
        }));
    }
}
