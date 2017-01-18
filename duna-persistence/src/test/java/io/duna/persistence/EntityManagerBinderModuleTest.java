/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;


public class EntityManagerBinderModuleTest {
    @Test
    public void testParsePersistenceUnitNames() throws Exception {
        EntityManagerBinderModule target = new EntityManagerBinderModule();
        InputStream persistenceXml = getClass().getResourceAsStream("/META-INF/persistence.xml");

        List<String> persistenceUnits = target.parsePersistenceUnitNames(persistenceXml);

        assertThat(persistenceUnits, hasItem("duna-test"));
    }

}
