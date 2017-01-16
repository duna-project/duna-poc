/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.proxy;

import io.duna.core.DunaException;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.duna.core.service.ServiceException;
import io.duna.core.util.Services;

import co.paralleluniverse.fibers.SuspendExecution;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import net.bytebuddy.implementation.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static io.vertx.ext.sync.Sync.awaitResult;

public class RemoteServiceCallInterceptor {

    private final Map<String, String> addressCache = new HashMap<>();

    @RuntimeType
    public Object intercept(@FieldValue("logger") Logger logger,
                            @FieldValue("objectMapper") ObjectMapper objectMapper,
                            @Origin Method method,
                            @This ServiceProxy proxyInstance,
                            @AllArguments Object ... args)
        throws SuspendExecution, InterruptedException {

        logger.fine(() -> "Intercepting call to " + method.toString());

        if (Vertx.currentContext() == null) {
            logger.severe(() -> "Proxy called outside vert.x context");
            throw new IllegalStateException("Proxy called outside vert.x context.");
        }

        String serviceAddress = addressCache.computeIfAbsent(proxyInstance.getQualifier() + method.toString(),
            k -> proxyInstance.getQualifier().isEmpty()
                ? Services.getInternalServiceAddress(method)
                : proxyInstance.getQualifier() + "@" + Services.getInternalServiceAddress(method));

        Vertx vertx = Vertx.currentContext().owner();
        BufferOutputStream outputStream = new BufferOutputStream(Buffer.buffer());

        try {
            JsonGenerator generator = objectMapper.getFactory()
                .createGenerator(outputStream, JsonEncoding.UTF8);

            generator.writeStartArray(args.length);
            for (Object arg : args) generator.writeObject(arg);
            generator.writeEndArray();

            generator.flush();
            generator.close();

            logger.info(() -> "Sending request to " + serviceAddress);

            Class<?> wrapperReturnType = Primitives.wrap(method.getReturnType());

            if (Void.class.isAssignableFrom(wrapperReturnType)) {
                vertx.eventBus().send(serviceAddress, outputStream.getBuffer());
                return null;
            } else {
                try {
                    Message<Buffer> response = awaitResult(
                        h -> vertx.eventBus().send(serviceAddress, outputStream.getBuffer(), h));

                    if (response.body() != null && response.body().length() > 0) {
                        BufferInputStream inputStream = new BufferInputStream(response.body());
                        JsonParser parser = objectMapper.getFactory().createParser(inputStream);

                        final Object result = parser.readValueAs(method.getReturnType());

                        parser.close();
                        outputStream.close();
                        inputStream.close();
                        return result;
                    } else {
                        throw new DunaException("The request resulted in an empty answer.");
                    }
                } catch (VertxException ex) {
                    if (ex.getCause() instanceof ServiceException) {
                        throw (ServiceException) ex.getCause();
                    } else if (ex.getCause() instanceof DunaException) {
                        throw (DunaException) ex.getCause();
                    } else {
                        throw new DunaException(ex.getCause());
                    }
                }
            }
        } catch (IOException ex) {
            throw new DunaException(ex);
        }
    }
}
