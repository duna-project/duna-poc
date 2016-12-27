/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject;

import io.duna.core.service.ServiceVerticle;
import io.duna.core.service.ServiceVerticleFactory;
import io.duna.core.service.handler.DefaultServiceHandler;
import io.duna.extend.PortVerticleFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import io.vertx.core.spi.VerticleFactory;

public class VerticleBinderModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<VerticleFactory> verticleFactoryBinder = Multibinder.newSetBinder(binder(),
            VerticleFactory.class);

        verticleFactoryBinder
            .addBinding()
            .to(ServiceVerticleFactory.class);

        verticleFactoryBinder
            .addBinding()
            .to(PortVerticleFactory.class);

        install(new FactoryModuleBuilder()
            .implement(ServiceVerticle.class, ServiceVerticle.class)
            .build(ServiceVerticle.BinderFactory.class));

        install(new FactoryModuleBuilder()
            .implement(DefaultServiceHandler.class, DefaultServiceHandler.class)
            .build(DefaultServiceHandler.BinderFactory.class));
    }
}
