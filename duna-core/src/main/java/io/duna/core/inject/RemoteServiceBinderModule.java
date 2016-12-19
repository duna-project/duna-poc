/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.UnsafeTypeLiteral;
import io.duna.core.context.ClasspathScanner;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RemoteServiceBinderModule extends AbstractModule {

    private static final Logger logger = LogManager
        .getLogManager().getLogger(RemoteServiceBinderModule.class.getName());

    private final WeakReference<ClasspathScanner> classpathScanner;

    public RemoteServiceBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = new WeakReference<>(classpathScanner);
    }

    @Override
    protected void configure() {
        logger.info(() -> "");

        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        final ClasspathScanner effectiveScanner = localClasspathScanner;

        effectiveScanner
            .getRemoteServices()
            .parallelStream()
            .forEach(contractClassName -> {
                Class<?> contractClass;

                try {
                    contractClass = Class.forName(contractClassName, false, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, e, () -> "Error while trying to bind contract to service proxy.");
                    return;
                }

                if (!contractClass.isInterface()) {
                    logger.warning(() -> "Unable to bind " + contractClass.getName() + ". " +
                        "Contracts must be declared as interfaces.");
                    return;
                }

                UnsafeTypeLiteral contractTypeLiteral = new UnsafeTypeLiteral(contractClass);


            });
    }
}
