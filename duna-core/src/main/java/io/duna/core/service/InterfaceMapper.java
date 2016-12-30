/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class InterfaceMapper extends SimpleModule {

    private static final Logger logger = LogManager.getLogManager()
        .getLogger(InterfaceMapper.class.getName());

    @Inject
    public InterfaceMapper(String name) {
        super(name, Version.unknownVersion());
        loadMappingConfiguration();
    }

    @SuppressWarnings("unchecked")
    private void loadMappingConfiguration() {
        Config config = ConfigFactory.load();
        ConfigObject mappings = config.getObject("duna.interface-mapping");

        logger.fine(() -> "Mapping interfaces to concrete implementations");

        mappings.entrySet().forEach(entry -> {
            Class<?> iface;
            try {
                iface = Class.forName(entry.getKey());

                if (!iface.isInterface()) {
                    logger.warning(() -> "Error while mapping " + entry.getKey() + ". " +
                        "Only interfaces can be mapped to concrete implementations.");
                    return;
                }
            } catch (ClassNotFoundException e) {
                logger.warning(() -> "Interface " + entry.getKey() + " not found.");
                return;
            }

            Class<?> impl;
            try {
                ConfigValue value = entry.getValue();
                String implName = value.render().replaceAll("\"", "");

                impl = Class.forName(implName);

                if (Modifier.isAbstract(impl.getModifiers()) || impl.isInterface()) {
                    logger.warning(() -> "Error while mapping " + entry.getValue() + ". " +
                        "Only concrete classes can be mapped to interfaces.");
                    return;
                }

                if (!iface.isAssignableFrom(impl)) {
                    logger.severe(() -> "Class " + impl.getName() + " doesn't implement interface " +
                        iface.getName() + "");
                    return;
                }
            } catch (ClassNotFoundException e) {
                logger.warning(() -> "Class " + entry.getValue().render() + " not found.");
                return;
            }

            logger.finer(() -> "Mapping interface " + iface.getName() + " to " + impl.getName());
            this.addAbstractTypeMapping((Class<Object>) iface, impl);
        });
    }
}
