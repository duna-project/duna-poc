/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.agent;

import io.duna.core.service.Contract;
import io.duna.instrument.AgentInstrumentationListener;
import io.duna.instrument.SuspendableInterfaceMethodsTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * The Duna java agent.
 *
 * @author <a href="mailto:ceduardo.melo@gmail.com">Carlos Eduardo Melo</a>
 */
public class DunaJavaAgent {

    private static final Logger logger = Logger.getLogger(DunaJavaAgent.class.getName());

    private static volatile boolean active;

    public static void premain(String args, Instrumentation instrumentation) {
        logger.fine(() -> "Attaching the Duna java agent at bootstrap");
        install(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        logger.fine(() -> "Attaching the Duna java agent to a running VM");
        install(args, instrumentation);
    }

    private static void install(String args, Instrumentation instrumentation) {
        if (!instrumentation.isRetransformClassesSupported()) {
            logger.severe(() -> "Class retransformation isn't supported by this VM");
        }

        ClassFileTransformer classFileTransformer = new AgentBuilder.Default()
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new AgentInstrumentationListener())
            .type(isAnnotatedWith(Contract.class).and(isInterface().or(isAbstract())))
            .transform(new SuspendableInterfaceMethodsTransformer())
            .installOn(instrumentation);

        active = true;
    }

    public static boolean isActive() {
        return active;
    }
}
