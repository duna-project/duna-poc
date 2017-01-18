/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import io.duna.core.context.ClasspathScanner;
import io.duna.core.proxy.ServiceProxyFactory;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.UnsafeTypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RemoteServicesBinderModule extends AbstractModule {

    private static final Logger logger = LogManager
        .getLogManager().getLogger(RemoteServicesBinderModule.class.getName());

    private final WeakReference<ClasspathScanner> classpathScanner;

    public RemoteServicesBinderModule(ClasspathScanner classpathScanner) {
        this.classpathScanner = new WeakReference<>(classpathScanner);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        logger.info(() -> "Binding proxies for remote services");

        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        final ClasspathScanner effectiveScanner = localClasspathScanner;

        Predicate<AnnotatedElement> isRemoteDependency = element -> effectiveScanner
            .getRemoteServices()
            .stream()
            .anyMatch(remoteService -> {
                String typeName;
                if (element instanceof Parameter) {
                    typeName = ((Parameter) element).getType().getName();
                } else if (element instanceof Field) {
                    typeName = ((Field) element).getType().getName();
                } else {
                    return false;
                }

                return Objects.equals(typeName, remoteService);
            });

        Set<InjectionPoint> localServicesInjectionPoints = getLocalServicesInjectionPoints();

        // Intercept executable injections
        localServicesInjectionPoints
            .stream()
            .map(InjectionPoint::getMember)
            .filter(member -> member instanceof Executable)
            .map(Executable.class::cast)
            .flatMap(executable -> Arrays.stream(executable.getParameters()))
            .filter(isRemoteDependency)
            .distinct()
            .forEach(this::bindAnnotatedElement);

        // Intercept field injections
        localServicesInjectionPoints
            .stream()
            .map(InjectionPoint::getMember)
            .filter(member -> member instanceof Field)
            .map(Field.class::cast)
            .filter(isRemoteDependency)
            .distinct()
            .forEach(this::bindAnnotatedElement);
    }

    private void bindAnnotatedElement(AnnotatedElement element) {
        Class<?> contractClass;

        if (element instanceof Parameter) {
            contractClass = ((Parameter) element).getType();
        } else if (element instanceof Field) {
            contractClass = ((Field) element).getType();
        } else {
            throw new IllegalArgumentException("The annotated element must be either a "
                + "Parameter or a Field.");
        }

        if (!contractClass.isInterface()) {
            logger.warning(() -> "Unable to bind " + contractClass.getName() + ". "
                + "Contracts must be declared as interfaces.");
            return;
        }

        UnsafeTypeLiteral contractTypeLiteral = new UnsafeTypeLiteral(contractClass);
        Class<?> proxyClass = ServiceProxyFactory.loadForContract(contractClass);

        bind(contractTypeLiteral)
            .to(proxyClass);

        logger.fine(() -> "Bound " + contractClass + " to " + proxyClass);

        Arrays.stream(element.getAnnotations())
            .map(Annotation::annotationType)
            .filter(Annotations::isBindingAnnotation)
            .forEach(qualifier -> bind(contractTypeLiteral)
                .annotatedWith(qualifier)
                .to(ServiceProxyFactory.loadForContract(contractClass, qualifier)));
    }

    private Set<InjectionPoint> getLocalServicesInjectionPoints() {
        ClasspathScanner localClasspathScanner = classpathScanner.get();
        if (localClasspathScanner == null) localClasspathScanner = new ClasspathScanner();

        return localClasspathScanner
            .getImplementationsList()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(s -> {
                try {
                    return Class.forName(s.getClassName(), false, this.getClass().getClassLoader());
                } catch (ClassNotFoundException ignored) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .map(UnsafeTypeLiteral::new)
            .flatMap(typeLiteral -> Sets.union(
                Sets.newHashSet(InjectionPoint.forConstructorOf(typeLiteral)),
                Sets.union(
                    InjectionPoint.forStaticMethodsAndFields(typeLiteral),
                    InjectionPoint.forInstanceMethodsAndFields(typeLiteral)
                )).stream()
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
}
