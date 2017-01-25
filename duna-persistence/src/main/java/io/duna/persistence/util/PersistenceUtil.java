/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.util;

import com.google.common.base.CaseFormat;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.duna.persistence.jpa.ContainerPersistenceUnitInfo;
import io.duna.persistence.EntityManagerBinderModule;

import javax.persistence.SharedCacheMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class PersistenceUtil {

    private static final Logger logger = Logger.getLogger(EntityManagerBinderModule.class.getName());

    public static List<PersistenceUnitInfo> getPersistenceUnits() {
        List<PersistenceUnitInfo> result = new ArrayList<>();

        List<String> entityClasses;

        {
            // Scope to free this scanner as soon as possible
            entityClasses = new PersistenceClasspathScanner()
                .getJpaEntities();
        }

        Config config = ConfigFactory.load();

        List<? extends Config> dataSourceConfigs = config.getConfigList("duna.persistence.data-sources");

        for (Config dataSourceConfig : dataSourceConfigs) {
            if (!dataSourceConfig.hasPath("connection-url")
                || !dataSourceConfig.hasPath("driver")
                || !dataSourceConfig.hasPath("name")) {
                logger.severe("Data source isn't configured properly: " +
                    "'connection-url', 'driver' and 'name' configurations are mandatory.");
                continue;
            }

            if (!dataSourceConfig.hasPath("properties.provider-class-name")) {
                logger.severe("A JPA persistence provider class name must be specified in " +
                    "'properties.provider-class-name'.");
                continue;
            }

            Properties properties = new Properties();
            if (dataSourceConfig.hasPath("properties")) {
                dataSourceConfig
                    .getConfig("properties")
                    .entrySet()
                    .forEach(entry -> {
                        properties.setProperty("dataSource."
                            + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entry.getKey()),
                            entry.getValue().toString());
                    });
            }

            HikariConfig hikariConfig = new HikariConfig(properties);
            hikariConfig.setJdbcUrl(dataSourceConfig.getString("connection-url"));
            hikariConfig.setDataSourceClassName(dataSourceConfig.getString("driver"));

            if (dataSourceConfig.hasPath("username")) {
                hikariConfig.setUsername(dataSourceConfig.getString("username"));
            }

            if (dataSourceConfig.hasPath("password")) {
                hikariConfig.setPassword(dataSourceConfig.getString("password"));
            }

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            ContainerPersistenceUnitInfo persistenceUnitInfo = new ContainerPersistenceUnitInfo();

            persistenceUnitInfo.setPersistenceUnitName(dataSourceConfig.getString("name"));
            persistenceUnitInfo.setPersistenceUnitTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
            persistenceUnitInfo.setExcludeUnlistedClasses(false);
            persistenceUnitInfo.setNonJtaDataSource(dataSource);
            persistenceUnitInfo.setPersistenceProviderClassName(dataSourceConfig.getString("provider"));
            persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.DISABLE_SELECTIVE);
            persistenceUnitInfo.setManagedClassNames(entityClasses);
            persistenceUnitInfo.setPersistenceProviderClassName(
                dataSourceConfig.getString("properties.provider-class-name"));

            result.add(persistenceUnitInfo);
        }

        return result;
    }
}
