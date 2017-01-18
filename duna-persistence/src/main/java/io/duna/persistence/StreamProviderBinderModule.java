/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence;

import com.google.inject.AbstractModule;
import io.duna.extend.spi.BindingModule;
import io.duna.persistence.jinq.JinqJpaStreamProvider;
import io.duna.persistence.jinq.JinqStreamProvider;

public class StreamProviderBinderModule extends AbstractModule implements BindingModule {
    @Override
    protected void configure() {
        bind(JinqStreamProvider.class)
            .to(JinqJpaStreamProvider.class);
    }
}
