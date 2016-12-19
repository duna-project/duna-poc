/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import io.duna.core.context.ClasspathScanner;
import io.duna.core.service.LocalServices;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.UnsafeTypeLiteral;
import com.google.inject.multibindings.MapBinder;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalServiceBinderModule extends AbstractModule {

    private static final Logger logger = LogManager
        .getLogManager()
        .getLogger(LocalServiceBinderModule.class.getName());

    private final ClasspathScanner classpathScanner;

    public LocalServiceBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = classpathScanner;
    }

    @Override
    protected void configure() {
        logger.info(() -> "");
        logger.fine(() -> "");

        MapBinder<Class<?>, Object> localServicesBinder = MapBinder
            .newMapBinder(binder(),
                new TypeLiteral<Class<?>>() {},
                new TypeLiteral<Object>() {},
                LocalServices.class);

        classpathScanner
            .getLocalServices()
            .parallelStream()
            .forEach(contractClassName -> {
                Class<?> contractClass;

                try {
                    contractClass = Class.forName(contractClassName, false, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, e, () -> "");
                    return;
                }

                if (!contractClass.isInterface()) {
                    logger.warning(() -> "Unable to bind " + contractClass.getName() + ". " +
                        "Contracts must be declared as interfaces.");
                    return;
                }

                TypeLiteral<?> contractTypeLiteral = new UnsafeTypeLiteral(contractClass);

                classpathScanner
                    .getImplementationsList()
                    .get(contractClassName)
                    .parallelStream()
                    .map(impl -> {
                        try {
                            return Class.forName(impl.getClassName(), false, this.getClass().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            logger.log(Level.WARNING, e, () -> "");
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(serviceClass -> {

                    });
            });


    }
}
