/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import com.google.inject.multibindings.Multibinder;
import io.duna.core.context.ClasspathScanner;
import io.duna.core.service.LocalServices;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.UnsafeTypeLiteral;
import com.google.inject.multibindings.MapBinder;
import io.duna.core.util.Services;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
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

    private final WeakReference<ClasspathScanner> classpathScanner;

    public LocalServiceBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = new WeakReference<>(classpathScanner);
    }

    @Override
    protected void configure() {
        logger.info(() -> "Configuring local services");
        logger.fine(() -> "Binding local service contracts");

        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        final ClasspathScanner effectiveScanner = localClasspathScanner;

        MapBinder<Class<?>, Object> localServicesBinder = MapBinder
            .newMapBinder(binder(),
                new TypeLiteral<Class<?>>() {},
                new TypeLiteral<Object>() {},
                LocalServices.class)
            .permitDuplicates();

        Multibinder<String> localServiceNamesBinder =
            Multibinder.newSetBinder(binder(), String.class, LocalServices.class);

        effectiveScanner
            .getLocalServices()
            .parallelStream()
            .forEach(contractClassName -> {
                Class<?> contractClass;

                try {
                    contractClass = Class.forName(contractClassName, false, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, e,
                        () -> "Error while trying to bind contract to service implementation.");
                    return;
                }

                if (!contractClass.isInterface()) {
                    logger.warning(() -> "Unable to bind " + contractClass.getName() + ". " +
                        "Contracts must be declared as interfaces.");
                    return;
                }

                UnsafeTypeLiteral contractTypeLiteral = new UnsafeTypeLiteral(contractClass);

                effectiveScanner
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
                        if (serviceClass.isInterface() || Modifier.isAbstract(serviceClass.getModifiers())) {
                            logger.warning(() -> "Unable to bind " + serviceClass.getName() + ". " +
                                "Services must be instantiable.");
                            return;
                        }

                        Annotation qualifier = Services.getQualifier(serviceClass);

                        if (qualifier == null) {
                            bind(contractTypeLiteral)
                                .to(serviceClass);

                            localServiceNamesBinder.addBinding()
                                .toInstance(contractClassName);
                        } else {
                            bind(contractTypeLiteral)
                                .annotatedWith(qualifier)
                                .to(serviceClass);

                            localServiceNamesBinder.addBinding()
                                .toInstance(contractClassName + "@"
                                    + qualifier.annotationType().getName());
                        }

                        localServicesBinder
                            .addBinding(contractClass)
                            .to(serviceClass);

                        logger.fine(() -> "Bound " + contractClass + " to " + serviceClass);
                    });
            });


    }
}
