/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service.handler;

import io.duna.core.implementation.MethodCallDelegator;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;

import co.paralleluniverse.fibers.Fiber;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;
import com.google.inject.assistedinject.Assisted;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import net.bytebuddy.description.method.MethodDescription;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.vertx.ext.sync.Sync.getContextScheduler;

public class DefaultServiceHandler implements Handler<Message<Buffer>> {

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

            parser.nextToken(); // START_ARRAY
            parser.nextToken(); // START_OBJECT
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                params[i] = parser.readValueAs(method.getParameterTypes()[i]);
            }

            logger.finer(() -> "Parameters received: " + Arrays.toString(params));

            Class<?> wrapperReturnType = Primitives.wrap(method.getReturnType());

            if (Void.class.isAssignableFrom(wrapperReturnType)) {
                // Don't send a response
                new Fiber<Void>(getContextScheduler(), () -> delegator.invoke(params)).start();
            } else {
                Object result = delegator.invoke(params);

                BufferOutputStream outputStream = new BufferOutputStream();
                objectMapper
                    .getFactory()
                    .createGenerator(outputStream, JsonEncoding.UTF8)
                    .writeObject(result);

                logger.finer(() -> "Sending result to request " + event.replyAddress());
                event.reply(outputStream.getBuffer());
            }
        } catch (IOException e) {
            // TODO improve error passing
            event.fail(500, "Internal error");
            logger.log(Level.WARNING, e, () -> "Error while processing request.");
        }
    }

    public interface BinderFactory {
        DefaultServiceHandler create(Object serviceInstance, Method method);
    }
}
