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
import io.duna.persistence.stream.QueryStreamProvider;
import org.jinq.jpa.JPAQueryStreamProvider;

public class StreamProviderBinderModule extends AbstractModule implements BindingModule {
    @Override
    protected void configure() {
        bind(QueryStreamProvider.class)
            .to(JPAQueryStreamProvider.class);
    }
}
