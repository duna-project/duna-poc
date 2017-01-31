/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service.handler;

import io.duna.core.DunaException;
import io.duna.core.implementation.MethodCallDelegator;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.duna.core.service.ServiceException;

import co.paralleluniverse.fibers.Fiber;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;
import com.google.inject.assistedinject.Assisted;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;
import net.bytebuddy.description.method.MethodDescription;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.vertx.ext.sync.Sync.getContextScheduler;

public class DefaultServiceHandler implements Handler<Message<Buffer>> {

    private static final int errorCode = 500;

    private final MethodCallDelegator delegator;

    private final ObjectMapper objectMapper;

    private final Logger logger;

    private final Method method;

    private final MethodDescription.ForLoadedMethod methodDescription;

    @Inject
    public DefaultServiceHandler(@Assisted Object serviceInstance,
                                 @Assisted Method method,
                                 ObjectMapper objectMapper,
                                 Logger logger) {
        this.objectMapper = objectMapper;
        this.logger = logger;
        this.delegator = MethodCallDelegator.to(serviceInstance, method);
        this.method = method;

        this.methodDescription = new MethodDescription.ForLoadedMethod(method);
    }

    @Override
    public void handle(Message<Buffer> event) {
        logger.fine(() -> "Handling request to " + method.toString());

        BufferInputStream inputStream = new BufferInputStream(event.body());

        try {
            JsonParser parser = objectMapper.getFactory().createParser(inputStream);

            Object[] params = new Object[methodDescription.getParameters().size()];

            parser.nextToken();
            parser.nextToken();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                params[i] = parser.readValueAs(method.getParameterTypes()[i]);
            }

            logger.finer(() -> "Parameters received: " + Arrays.toString(params));

            Class<?> wrapperReturnType = Primitives.wrap(method.getReturnType());

            if (Void.class.isAssignableFrom(wrapperReturnType)) {
                // Don't send a response
                new Fiber<Void>(getContextScheduler(), () -> delegator.invoke(params)).start();
            } else {
                Fiber<Object> operation = new Fiber<>(Sync.getContextScheduler(), () -> delegator.invoke(params));

                operation.setUncaughtExceptionHandler((t, ex) -> {
                    logger.log(Level.SEVERE, ex, () -> "Service execution error:");

                    int failureCode;
                    if (ex instanceof ServiceException) {
                        failureCode = 0;
                    } else if (ex instanceof DunaException) {
                        failureCode = 1;
                    } else {
                        failureCode = 2;
                    }

                    event.fail(failureCode, ex.toString());
                });

                try {
                    Object result = operation.start().get();

                    BufferOutputStream outputStream = new BufferOutputStream();
                    objectMapper
                        .getFactory()
                        .createGenerator(outputStream, JsonEncoding.UTF8)
                        .writeObject(result);

                    logger.finer(() -> "Sending result to request " + event.replyAddress());
                    event.reply(outputStream.getBuffer());
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, ex, () -> "Computation interrupted:");
                } catch (ExecutionException ignored) {
                }
            }
        } catch (IOException ex) {
            // TODO improve error passing
            event.fail(errorCode, "Internal error");
            logger.log(Level.WARNING, ex, () -> "Error while processing request.");
        }
    }

    public interface BinderFactory {
        DefaultServiceHandler create(Object serviceInstance, Method method);
    }
}
