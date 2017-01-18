/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.instrument;

import io.duna.agent.DunaJavaAgent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Listens and logs instrumentation operations.
 *
 * @author <a href="mailto:ceduardo.melo@gmail.com">Carlos Eduardo Melo</a>
 */
public class AgentInstrumentationListener implements AgentBuilder.Listener {

    private static final Logger logger = LogManager.getLogManager()
        .getLogger(DunaJavaAgent.class.getName());

    @Override
    public void onTransformation(TypeDescription typeDescription,
                                 ClassLoader classLoader,
                                 JavaModule module,
                                 DynamicType dynamicType) {
        logger.fine(() -> String.format("%s will be instrumented", typeDescription));
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        logger.finest(() -> String.format("Instrumentation ignored type %s", typeDescription));
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader,
                        JavaModule module, Throwable throwable) {
        logger.warning(() -> String.format("Error while instrumenting %s", typeName));
        logger.fine(() -> {
            StringWriter errorWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorWriter));

            return errorWriter.toString();
        });
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
        logger.finest(() -> String.format("Instrumentation of %s complete", typeName));
    }
}
