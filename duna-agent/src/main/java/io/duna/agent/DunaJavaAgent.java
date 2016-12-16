/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.agent;

import io.duna.core.service.Contract;
import io.duna.instrument.AgentInstrumentationListener;
import io.duna.instrument.SuspendableMethodsTransformer;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.ref.WeakReference;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * The Duna java agent.
 *
 * @author <a href="mailto:ceduardo.melo@gmail.com">Carlos Eduardo Melo</a>
 */
public class DunaJavaAgent {

    private static final Logger LOGGER = LogManager.getLogManager()
        .getLogger(DunaJavaAgent.class.getName());

    private static volatile boolean ACTIVE = false;

    private static WeakReference<ClassFileTransformer> classFileTransformer;

    private static WeakReference<Instrumentation> instrumentation;

    public static void premain(String args, Instrumentation instrumentation) {
        LOGGER.fine(() -> "Attaching the Duna java agent at bootstrap");
        install(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        LOGGER.fine(() -> "Attaching the Duna java agent to a running VM");
        install(args, instrumentation);
    }

    private static void install(String args, Instrumentation instrumentation) {
        if (!instrumentation.isRetransformClassesSupported())
            LOGGER.severe(() -> "Class retransformation isn't supported by this VM");

        classFileTransformer = new WeakReference<ClassFileTransformer>(new AgentBuilder.Default()
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new AgentInstrumentationListener())
            .type(isAnnotatedWith(Contract.class).and(isInterface().or(isAbstract())))
            .transform(new SuspendableMethodsTransformer())
            .installOn(instrumentation));

        ACTIVE = true;

        DunaJavaAgent.instrumentation = new WeakReference<>(instrumentation);
    }

    public static Instrumentation getInstrumentation() {
        if (instrumentation == null)
            throw new UnsupportedOperationException("Agent not installed");

        return instrumentation.get();
    }

    public static ClassFileTransformer getClassFileTransformer() {
        if (classFileTransformer == null)
            throw new UnsupportedOperationException("Agent not installed");

        return classFileTransformer.get();
    }

    public static boolean isActive() {
        return ACTIVE;
    }
}
