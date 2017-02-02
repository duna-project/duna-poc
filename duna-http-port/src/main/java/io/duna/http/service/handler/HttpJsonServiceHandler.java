/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.duna.core.DunaException;
import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.duna.core.util.Services;
import io.duna.http.HttpResponseStatus;
import io.duna.http.Parameter;
import io.duna.serialization.Json;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import kotlinx.reflect.lite.ClassMetadata;
import kotlinx.reflect.lite.FunctionMetadata;
import kotlinx.reflect.lite.ParameterMetadata;
import kotlinx.reflect.lite.ReflectionLite;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HttpJsonServiceHandler implements Handler<RoutingContext> {

    private final Logger logger;

    private final ObjectMapper externalObjectMapper;

    private final ObjectMapper internalObjectMapper;

    private final Method targetMethod;

    private final Map<String, Integer> parameterIndexes;

    private final Map<String, Class<?>> parameterTypes;

    private final Config config = ConfigFactory.load();

    private final String corsDomains = config.getStringList("duna.web.cors")
        .stream()
        .collect(Collectors.joining(","));

    @Inject
    public HttpJsonServiceHandler(@Assisted Class<?> contractClass,
                                  @Assisted Method method,
                                  @Assisted String path,
                                  Logger logger,
                                  @Json ObjectMapper externalObjectMapper,
                                  ObjectMapper internalObjectMapper) {
        this.logger = logger;
        this.externalObjectMapper = externalObjectMapper;
        this.internalObjectMapper = internalObjectMapper;

        this.targetMethod = method;
        this.parameterIndexes = new HashMap<>(method.getParameterCount());
        this.parameterTypes = new HashMap<>(method.getParameterCount());

        ClassMetadata classMetadata = ReflectionLite.INSTANCE.loadClassMetadata(method.getDeclaringClass());

        if (classMetadata != null) {
            parseKotlinParameterNames(classMetadata, method);
        } else {
            parseJavaParameterNames(method);
        }
    }

    @Suspendable
    @Override
    public void handle(RoutingContext event) {
        logger.fine(() -> "Handling request to "
            + Services.getInternalServiceAddress(targetMethod));

        if (event.getBody() != null && event.getBody().length() > 0 && event.request().method() == HttpMethod.GET) {
            logger.info(() -> "Invalid request from " + event.request().remoteAddress() + ". "
                + "GET requests don't provide bodies.");

            event.response()
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .setStatusMessage("Invalid request. GET requests can't provide a body.")
                .end();

            return;
        }

        Object[] parameterValues = new Object[targetMethod.getParameterCount()];

        // Parse path parameters
        event.request().params().forEach(entry -> {
            if (!parameterIndexes.containsKey(entry.getKey())) {
                logger.fine(() -> "Parameter " + entry.getKey() + " not mapped.");
                return;
            }

            int paramIndex = parameterIndexes.get(entry.getKey());
            logger.finer(() -> "Setting parameter " + paramIndex + " with value " + entry.getValue());

            if (parameterTypes.get(entry.getKey()).equals(String.class)) {
                parameterValues[paramIndex] = entry.getValue();
            } else {
                try {
                    parameterValues[paramIndex] = internalObjectMapper
                        .readValue(entry.getValue(), parameterTypes.get(entry.getKey()));
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex, () -> "Error while deserializing parameter " + entry.getKey());
                }
            }
        });

        // Parse request body to get the remaining parameterIndexes
        if (event.getBody() != null
            && "application/json".equals(event.request().getHeader("content-type"))) {
            parseBodyParameters(event, parameterValues);
        }

        sendRequestToService(event, parameterValues);
    }

    private Object[] parseBodyParameters(RoutingContext event, Object[] parameterValues) {
        try {
            logger.fine(() -> "Processing request body " + event.getBody());

            BufferInputStream inputStream = new BufferInputStream(event.getBody());
            JsonParser parser = externalObjectMapper.getFactory().createParser(inputStream);

            JsonNode parametersTree = parser.readValueAsTree();

            parametersTree.fields().forEachRemaining(e -> {
                Class<?> paramType = parameterTypes.get(e.getKey());
                int paramIndex = parameterIndexes.get(e.getKey());

                try {
                    Object paramValue = externalObjectMapper.treeToValue(e.getValue(), paramType);

                    logger.finer(() -> "Setting parameter " + paramIndex + " with value " + paramValue);
                    parameterValues[paramIndex] = paramValue;
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex, () -> "Error while parsing the parameters tree.");
                }
            });

            return parameterValues;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex, () -> "Error while handling request from "
                + event.request().remoteAddress());
            event.response()
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .end();
            throw new DunaException("Error while parsing parameter values");
        }
    }

    @Suspendable
    private void sendRequestToService(RoutingContext event, Object[] parameterValues) {
        String serviceAddress = Services.getInternalServiceAddress(targetMethod);

        try {
            BufferOutputStream requestOutputStream = new BufferOutputStream(Buffer.buffer());
            JsonGenerator requestGenerator = internalObjectMapper.getFactory().createGenerator(requestOutputStream);

            requestGenerator.writeStartArray();
            for (Object arg : parameterValues) requestGenerator.writeObject(arg);
            requestGenerator.writeEndArray();

            requestGenerator.flush();
            requestGenerator.close();

            event.vertx().eventBus().<Buffer>send(serviceAddress, requestOutputStream.getBuffer(), serviceResponse -> {
                if (serviceResponse.failed()) {
                    handleReplyException(event, serviceResponse.cause());
                    return;
                }

                Class<?> wrapperReturnType = Primitives.wrap(targetMethod.getReturnType());

                if (Void.class.isAssignableFrom(wrapperReturnType)) {
                    // TODO Maybe move this to before the event dispatching
                    event.response().end();
                }

                if (serviceResponse.result().body() != null && serviceResponse.result().body().length() > 0) {
                    try {
                        logger.finer(() -> "Received response from service: " + serviceAddress);

                        JsonParser parser = internalObjectMapper.getFactory()
                                                                .createParser(serviceResponse.result()
                                                                                             .body()
                                                                                             .getBytes());

                        Object result = parser.readValueAs(targetMethod.getReturnType());

                        event.response()
                             .putHeader("content-type", "text/json")
                             .putHeader("access-control-allow-origin", corsDomains)
                             .setChunked(true);

                        if (result instanceof Iterable) {
                            // Serialize iterable items individually in order to reduce memory usage
                            event.response().write("[");

                            Iterable<?> items = (Iterable<?>) result;
                            boolean first = true;
                            for (Object item : items) {
                                if (!first) {
                                    event.response().write(",");
                                }
                                first = false;

                                event.response().write(externalObjectMapper.writeValueAsString(item));
                            }

                            event.response().write("]");
                        } else {
                            event.response().write(externalObjectMapper.writeValueAsString(result));
                        }

                        event.response().end();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, ex, () -> "Error while handling request from " + event.request().remoteAddress());
                        event.response()
                            .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                            .end();
                    }
                } else {
                    event.response().end();
                }
            });
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex, () -> "Error while handling request from " + event.request().remoteAddress());
            event.response()
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .end();
        }
    }

    private void handleReplyException(RoutingContext event, Throwable cause) {
        if (cause instanceof ReplyException) {
            ReplyException ex = (ReplyException) cause;

            Level logLevel = ex.failureCode() == 0 ? Level.FINE : Level.SEVERE;
            logger.log(logLevel, ex, () -> "Request to service failed.");

            switch (ex.failureCode()) {
                case 0: // ServiceException
                    event.response()
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .setChunked(true)
                        .write(ex.getMessage())
                        .end();
                    break;
                case 1:
                case 2:
                    event.response()
                        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .end();
            }
        }
    }

    private void parseKotlinParameterNames(ClassMetadata classMetadata, Method method) {
        FunctionMetadata functionMetadata = classMetadata.getFunction(method);

        if (functionMetadata != null) {
            @SuppressWarnings("unchecked")
            List<ParameterMetadata> parametersMetadata = (List<ParameterMetadata>) functionMetadata.getParameters();

            for (int i = 0; i < method.getParameterCount(); i++) {
                parameterIndexes.put(parametersMetadata.get(i).getName(), i);
                parameterTypes.put(parametersMetadata.get(i).getName(), method.getParameters()[i].getType());
            }
        }
    }

    private void parseJavaParameterNames(Method method) {
        boolean paramAnnotationNotPresent = false;
        for (int i = 0; i < method.getParameterCount(); i++) {
            java.lang.reflect.Parameter param = method.getParameters()[i];

            if (!param.isAnnotationPresent(Parameter.class)) {
                paramAnnotationNotPresent = true;
                break;
            }

            parameterIndexes.put(param.getAnnotation(Parameter.class).value(), i);
            parameterTypes.put(param.getAnnotation(Parameter.class).value(), param.getType());
        }

        if (paramAnnotationNotPresent) {
            boolean parameterUseDefaultJvmNaming = false;
            for (int i = 0; i < method.getParameterCount(); i++) {
                if (method.getParameters()[i].getName().equals("arg" + i)) {
                    parameterUseDefaultJvmNaming = true;
                    break;
                }

                parameterIndexes.put(method.getParameters()[i].getName(), i);
                parameterTypes.put(method.getParameters()[i].getName(), method.getParameters()[i].getType());
            }

            if (parameterUseDefaultJvmNaming) {
                logger.warning(() ->
                    "The service " + method.getDeclaringClass()
                        + " does not have any parameter name information. Either compile it"
                        + " using the -parameters flag (Java 8+), or annotate the parameters"
                        + " with @Parameter."
                );
            }
        }
    }

    public interface BinderFactory {
        HttpJsonServiceHandler create(Class<?> contractClass,
                                      Method method,
                                      String path);
    }
}
