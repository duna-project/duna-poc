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
import io.duna.core.util.Services;

import com.google.inject.AbstractModule;
import com.google.inject.UnsafeTypeLiteral;
import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LocalServicesBinderModule extends AbstractModule {

    private static final Logger logger = LogManager
        .getLogManager()
        .getLogger(LocalServicesBinderModule.class.getName());

    private final WeakReference<ClasspathScanner> classpathScanner;

    public LocalServicesBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = new WeakReference<>(classpathScanner);
    }

    @Override
    protected void configure() {
        logger.info(() -> "Configuring local services");
        logger.fine(() -> "Binding local service contracts");

//        MapBinder<Class<?>, Object> localServicesBinder = MapBinder
//            .newMapBinder(binder(),
//                new TypeLiteral<Class<?>>() {},
//                new TypeLiteral<Object>() {},
//                LocalServices.class)
//            .permitDuplicates();

        Multibinder<String> localServiceNamesBinder =
            Multibinder.newSetBinder(binder(), String.class, LocalServices.class);

        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        final ClasspathScanner effectiveScanner = localClasspathScanner;
        effectiveScanner
            .getLocalServices()
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
                    .stream()
                    .map(impl -> {
                        try {
                            return Class.forName(impl.getClassName(), false, this.getClass().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            logger.log(Level.WARNING, e, () -> "Can't find class " + impl.getClassName()
                                + " in the classpath.");
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

                        Class<? extends Annotation> qualifier = Services.getQualifier(serviceClass);

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
                                .toInstance(qualifier.getName() + "@" + contractClassName);
                        }

//                        localServicesBinder
//                            .addBinding(contractClass)
//                            .to(serviceClass);

                        logger.fine(() -> "Bound " + contractClass + " to " + serviceClass);
                    });
            });
    }
}
