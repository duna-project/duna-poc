package io.duna.web.service.handler;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;
import io.duna.core.implementation.MethodCallDelegator;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestServiceHandler<T> implements Handler<RoutingContext> {

    private final T service;

    private final MethodCallDelegator<T> methodCallDelegator;

    private final Logger logger;

    private final ObjectMapper objectMapper;

    @Inject
    public RestServiceHandler(@Assisted T service,
                              @Assisted Method method,
                              Logger logger,
                              ObjectMapper objectMapper) {
        this.service = service;
        this.methodCallDelegator = MethodCallDelegator.Companion.to(service, method);
        this.logger = logger;
        this.objectMapper = objectMapper;
    }

    @Suspendable
    @Override
    public void handle(RoutingContext event) {
        System.out.println("Got a request " + event.toString());
        event.response().end("ASDasd");
    }

    public interface BinderFactory {
        RestServiceHandler<Object> create(Object service, Method method);
    }
}
