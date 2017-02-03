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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.io.IoBuilder;

import javax.persistence.SharedCacheMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

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
            if (!dataSourceConfig.hasPath("name")) {
                logger.severe("Data source isn't configured properly: a name must be provided.");
                continue;
            }

            if (!dataSourceConfig.hasPath("jpa-properties.provider-class-name")) {
                logger.severe("A JPA persistence provider class name must be specified in " +
                    "'jpa-properties.provider-class-name'.");
                continue;
            }

            if (dataSourceConfig.hasPath("ds-properties.data-source-class-name") &&
                dataSourceConfig.hasPath("connection-url")) {
                logger.severe("'connection-url' and 'ds-properties.data-source-class-name' "
                    + "configurations can't be mixed.");
                continue;
            }

            Properties properties = new Properties();
            if (dataSourceConfig.hasPath("ds-properties")) {
                dataSourceConfig
                    .getConfig("ds-properties")
                    .entrySet()
                    .forEach(entry -> properties.setProperty(
                        CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, entry.getKey()),
                        entry.getValue().unwrapped().toString()));

                if (dataSourceConfig.hasPath("ds-properties.data-source-class-name")) {
                    if (dataSourceConfig.hasPath("username")) {
                        properties.setProperty("dataSource.user", dataSourceConfig.getString("username"));
                    }

                    if (dataSourceConfig.hasPath("password")) {
                        properties.setProperty("dataSource.password",
                            dataSourceConfig.getString("password"));
                    }

                    if (dataSourceConfig.hasPath("database")) {
                        properties.setProperty("dataSource.databaseName",
                            dataSourceConfig.getString("database"));
                    }

                    if (dataSourceConfig.hasPath("server")) {
                        properties.setProperty("dataSource.serverName",
                            dataSourceConfig.getString("server"));
                    }

                    if (dataSourceConfig.hasPath("port")) {
                        properties.setProperty("dataSource.port",
                            dataSourceConfig.getString("port"));
                    }
                }
            }

            HikariConfig hikariConfig = new HikariConfig(properties);

            if (dataSourceConfig.hasPath("connection-url")) {
                hikariConfig.setJdbcUrl(dataSourceConfig.getString("connection-url"));
            }

            if (dataSourceConfig.hasPath("username")) {
                hikariConfig.setUsername(dataSourceConfig.getString("username"));
            }

            if (dataSourceConfig.hasPath("password")) {
                hikariConfig.setPassword(dataSourceConfig.getString("password"));
            }

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            try {
                dataSource.setLogWriter(IoBuilder
                    .forLogger(PersistenceUtil.class)
                    .setLevel(Level.DEBUG)
                    .buildPrintWriter());
            } catch (SQLException ignored) {
                logger.warning(() -> "Could'nt setup a logger for data source "
                    + dataSourceConfig.getString("name"));
            }

            Config jpaConfig = dataSourceConfig.getConfig("jpa-properties");

            ContainerPersistenceUnitInfo persistenceUnitInfo = new ContainerPersistenceUnitInfo();

            persistenceUnitInfo.setPersistenceUnitName(dataSourceConfig.getString("name"));
            persistenceUnitInfo.setPersistenceUnitTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
            persistenceUnitInfo.setExcludeUnlistedClasses(false);
            persistenceUnitInfo.setNonJtaDataSource(dataSource);
            persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.DISABLE_SELECTIVE);
            persistenceUnitInfo.setManagedClassNames(entityClasses);
            persistenceUnitInfo.setPersistenceProviderClassName(jpaConfig.getString("provider-class-name"));

            if (jpaConfig.hasPath("other-properties")) {
                Properties otherProperties = new Properties();
                jpaConfig.getConfig("other-properties")
                    .entrySet()
                    .forEach(entry -> otherProperties.setProperty(
                        entry.getKey(), entry.getValue().unwrapped().toString()
                    ));
                persistenceUnitInfo.setProperties(otherProperties);
            }

            result.add(persistenceUnitInfo);
        }

        return result;
    }
}
