/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http.service.handler;

import io.duna.core.io.BufferInputStream;
import io.duna.core.io.BufferOutputStream;
import io.duna.core.util.Services;
import io.duna.http.Parameter;
import io.duna.http.util.Paths;
import io.duna.serialization.Json;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Primitives;
import com.google.inject.assistedinject.Assisted;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import kotlinx.reflect.lite.ClassMetadata;
import kotlinx.reflect.lite.FunctionMetadata;
import kotlinx.reflect.lite.ParameterMetadata;
import kotlinx.reflect.lite.ReflectionLite;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HttpJsonServiceHandler<T> implements Handler<RoutingContext> {

    private final Logger logger;

    private final ObjectMapper externalObjectMapper;

    private final ObjectMapper internalObjectMapper;

    private final Method targetMethod;

    private final Map<String, Integer> parameterIndexes;

    private final Map<String, Class<?>> parameterTypes;

    private final Set<String> pathParameters;

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

        pathParameters = Paths.getPathParameters(path)
            .stream()
            .collect(Collectors.toCollection(HashSet<String>::new));
    }

    @Suspendable
    @Override
    public void handle(RoutingContext event) {
        logger.fine(() -> "Handling request to "
            + Services.getInternalServiceAddress(targetMethod));

        if (event.getBody() != null && event.getBody().length() > 0 && event.request().method() == HttpMethod.GET) {
            logger.info(() -> "Invalid request from " + event.request().remoteAddress() + ". " +
                "GET requests don't provide bodies.");

            event.response()
                .setStatusCode(403)
                .setStatusMessage("Invalid request. GET requests can't provide a body.")
                .end();

            return;
        }

        Object[] parameterValues = new Object[targetMethod.getParameterCount()];

        // Parse path parameterIndexes
        for (String param : pathParameters) {
            int index = parameterIndexes.get(param);
            String value = event.request().getParam(param);

            parameterValues[index] = value;
        }

        // Parse request body to get the remaining parameterIndexes
        if (event.getBody() != null) {
            try {
                BufferInputStream inputStream = new BufferInputStream(event.getBody());
                JsonParser parser = externalObjectMapper.getFactory().createParser(inputStream);

                while (parser.hasToken(JsonToken.FIELD_NAME)) {
                    String paramName = parser.nextFieldName();
                    Class<?> paramType = parameterTypes.get(paramName);
                    int paramIndex = parameterIndexes.get(paramName);
                    Object paramValue = parser.readValueAs(paramType);

                    parameterValues[paramIndex] = paramValue;
                }
            } catch (IOException e) {
                event.response()
                    .setStatusCode(500)
                    .setStatusMessage("Internal Server Error: " + e.getLocalizedMessage())
                    .end();
                return;
            }
        }

        sendRequestToService(event, parameterValues);
    }

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

            event.vertx().eventBus().<Buffer> send(serviceAddress, requestOutputStream.getBuffer(), res -> {
                if (res.failed()) {
                    event.response()
                        .setStatusCode(500)
                        .setStatusMessage(res.cause().getLocalizedMessage())
                        .end();
                    return;
                }

                Class<?> wrapperReturnType = Primitives.wrap(targetMethod.getReturnType());

                if (Void.class.isAssignableFrom(wrapperReturnType)) {
                    // TODO Maybe move this to before the event dispatching
                    event.response().end();
                }

                try {
                    BufferInputStream responseInputStream = new BufferInputStream(res.result().body());
                    JsonNode responseJson = internalObjectMapper
                        .reader()
                        .readTree(responseInputStream);

                    String response = externalObjectMapper
                        .writer()
                        .writeValueAsString(responseJson);

                    event.response()
                        .end(response);
                } catch (IOException e) {
                    event.response()
                        .setStatusCode(500)
                        .setStatusMessage("Internal Server Error: " + e.getLocalizedMessage())
                        .end();
                }
            });
        } catch (IOException e) {
            event.response()
                .setStatusCode(500)
                .setStatusMessage("Internal Server Error: " + e.getLocalizedMessage())
                .end();
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
                    "The service " + method.getDeclaringClass() +
                        " does not have any parameter name information. Either compile it" +
                        " using the -parameterIndexes flag (Java 8+), or annotate the parameterIndexes" +
                        " with @Parameter."
                );
            }
        }
    }

    public interface BinderFactory {
        HttpJsonServiceHandler<Object> create(Class<?> contractClass,
                                              Method method,
                                              String path);
    }
}
