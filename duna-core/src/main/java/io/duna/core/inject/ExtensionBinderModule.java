/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.duna.core.context.ClasspathScanner;
import io.duna.extend.Port;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

public class ExtensionBinderModule extends AbstractModule {

    private static final Logger logger = Logger.getLogger(ExtensionBinderModule.class.getName());

    private final WeakReference<ClasspathScanner> classpathScanner;

    public ExtensionBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = new WeakReference<>(classpathScanner);
    }

    @Override
    protected void configure() {
        logger.info(() -> "Configuring extensions");

        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        Multibinder<String> portClassNames = Multibinder.newSetBinder(binder(), String.class, Port.class);

        localClasspathScanner
            .getPortExtensions()
            .forEach(ext -> portClassNames.addBinding().toInstance(ext));
    }
}
