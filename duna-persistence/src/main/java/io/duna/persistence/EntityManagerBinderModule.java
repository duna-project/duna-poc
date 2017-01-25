/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */

package io.duna.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.duna.extend.spi.BindingModule;
import io.duna.persistence.util.PersistenceUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityManagerBinderModule extends AbstractModule implements BindingModule {

    private final Logger logger = Logger.getLogger(EntityManagerBinderModule.class.getName());

    @Override
    protected void configure() {
        logger.info(() -> "Binding persistence extensions");

        InputStream persistenceXmlStream = getClass().getResourceAsStream("/META-INF/persistence.xml");

        if (persistenceXmlStream == null) {
            logger.severe(() -> "No persistence.xml configuration found in the classpath.");
            return;
        }

        List<PersistenceUnitInfo> persistenceUnitInfos = PersistenceUtil.getPersistenceUnits();

        if (persistenceUnitInfos.isEmpty())
            logger.warning(() -> "No data sources configured.");

        boolean firstDeclaredPersistenceUnit = true;
        for (PersistenceUnitInfo persistenceUnitInfo : persistenceUnitInfos) {
            try {
                logger.fine(() -> "Binding entity manager for " + persistenceUnitInfo.getPersistenceUnitName());

                PersistenceProvider provider;

                try {
                    Class<?> providerClass = Class.forName(persistenceUnitInfo.getPersistenceProviderClassName());
                    provider = (PersistenceProvider) providerClass.newInstance();
                } catch (ClassNotFoundException e) {
                    logger.severe("Persistence provider " + persistenceUnitInfo.getPersistenceProviderClassName()
                        + " not found.");
                    continue;
                } catch (IllegalAccessException | InstantiationException ex) {
                    logger.log(Level.SEVERE, ex, () -> "Error while trying to instantiate the persistence provider.");
                    continue;
                }

                EntityManagerFactory entityManagerFactory = provider
                    .createContainerEntityManagerFactory(persistenceUnitInfo, Collections.emptyMap());

                if (firstDeclaredPersistenceUnit) {
                    firstDeclaredPersistenceUnit = false;

                    bind(EntityManagerFactory.class)
                        .toInstance(entityManagerFactory);

                    bind(EntityManager.class)
                        .toProvider(entityManagerFactory::createEntityManager);
                }

                bind(EntityManagerFactory.class)
                    .annotatedWith(Names.named(persistenceUnitInfo.getPersistenceUnitName()))
                    .toInstance(entityManagerFactory);

                bind(EntityManager.class)
                    .annotatedWith(Names.named(persistenceUnitInfo.getPersistenceUnitName()))
                    .toProvider(entityManagerFactory::createEntityManager);
            } catch (PersistenceException exception) {
                logger.log(Level.SEVERE, exception, () -> "Error while creating entity manager factory");
                return;
            }
        }

        install(new StreamProviderBinderModule());
    }
}
