/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.service.handler;

import io.duna.core.implementation.MethodCallDelegator;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;
import io.duna.http.Param;
import io.duna.serialization.Json;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import kotlin.Metadata;
import kotlinx.reflect.lite.ClassMetadata;
import kotlinx.reflect.lite.ReflectionLite;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HttpServiceHandler<T> implements Handler<RoutingContext> {

    private final String address;

    private final T service;

    private final MethodCallDelegator<T> methodCallDelegator;

    private final Logger logger;

    private final ObjectMapper objectMapper;

    private final Map<String, Class<?>> parameters;

    private final List<String> parameterOrder;

    @Inject
    public HttpServiceHandler(@Assisted T service,
                              @Assisted Method method,
                              Logger logger,
                              @Json ObjectMapper objectMapper) {
        this.service = service;
        this.methodCallDelegator = MethodCallDelegator.Companion.to(service, method);
        this.logger = logger;
        this.objectMapper = objectMapper;

        this.parameterOrder = new ArrayList<>(method.getParameterCount());
        this.parameters = new HashMap<>(method.getParameterCount());

        if (method.getDeclaringClass().isAnnotationPresent(Metadata.class)) {
            ClassMetadata metadata = ReflectionLite.INSTANCE.loadClassMetadata(method.getDeclaringClass());
        }

        for (Parameter param : method.getParameters()) {
            Param paramAnnotation = param.getAnnotation(Param.class);

            if (paramAnnotation != null) {
                parameterOrder.add(paramAnnotation.value());
            } else {
//                if ()

            }
        }

        this.address = method.getDeclaringClass().getName() + "." +
            method.getName() + "(" +
            Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(",")) +
            ")";
    }

    /**
     * Order of parameter handling:
     *   - Path parameter
     *   - Form parameter
     *   - Query parameter
     * @param event
     */
    @Suspendable
    @Override
    public void handle(RoutingContext event) {
        logger.fine(() -> "Handling request to " + address);



        if (event.getBody() != null) {
            logger.finer(() -> "Request with body");
        }

//        InputStream inputStream = new BufferInputStream(event.getBody());

        System.out.println(event.request().getParam("test"));

        System.out.println("Got a request " + event.toString());
        event.response().end("ASDasd");
    }

    public interface BinderFactory {
        HttpServiceHandler<Object> create(Object service, Method method);
    }
}
