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
import io.duna.persistence.jinq.JinqJpaStreamProvider;
import org.jinq.jpa.JinqJPAStreamProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class EntityManagerBinderModule extends AbstractModule implements BindingModule {

    private final Logger logger = LogManager.getLogManager().getLogger(EntityManagerBinderModule.class.getName());

    @Override
    protected void configure() {
        logger.info(() -> "Binding persistence extensions");

        InputStream persistenceXmlStream = getClass().getResourceAsStream("/META-INF/persistence.xml");

        if (persistenceXmlStream == null) {
            logger.severe(() -> "No persistence.xml configuration found in the classpath.");
            return;
        }

        List<String> persistenceUnitNames = parsePersistenceUnitNames(persistenceXmlStream);

        if (persistenceUnitNames.isEmpty())
            logger.warning(() -> "No persistence units found in persistence.xml");

        boolean firstDeclaredPersistenceUnit = true;
        for (String persistenceUnit : persistenceUnitNames) {
            try {
                logger.fine(() -> "Binding entity manager for " + persistenceUnit);

                EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);

                if (firstDeclaredPersistenceUnit) {
                    firstDeclaredPersistenceUnit = false;

                    bind(EntityManagerFactory.class)
                        .toInstance(entityManagerFactory);

                    bind(EntityManager.class)
                        .toProvider(entityManagerFactory::createEntityManager);
                }

                bind(EntityManagerFactory.class)
                    .annotatedWith(Names.named(persistenceUnit))
                    .toInstance(entityManagerFactory);

                bind(EntityManager.class)
                    .annotatedWith(Names.named(persistenceUnit))
                    .toProvider(entityManagerFactory::createEntityManager);
            } catch (PersistenceException exception) {
                logger.log(Level.SEVERE, exception, () -> "Error while creating entity manager factory");
                return;
            }
        }

        install(new StreamProviderBinderModule());
    }

    List<String> parsePersistenceUnitNames(InputStream persistenceXmlStream) {
        List<String> persistenceUnitNames = new ArrayList<>();

        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(persistenceXmlStream);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.getEventType() == START_ELEMENT) {
                    StartElement element = event.asStartElement();

                    if (!element.getName().getNamespaceURI().equals("http://xmlns.jcp.org/xml/ns/persistence") ||
                        !element.getName().getLocalPart().equals("persistence-unit"))
                        continue;

                    persistenceUnitNames.add(element
                        .getAttributeByName(new QName("name"))
                        .getValue());
                }
            }
        } catch (XMLStreamException xmlException) {
            logger.log(Level.SEVERE, xmlException, () -> "Error while parsing persistence.xml");
        }

        return persistenceUnitNames;
    }
}
