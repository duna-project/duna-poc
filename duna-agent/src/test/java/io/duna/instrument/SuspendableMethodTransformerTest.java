/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.instrument;

import co.paralleluniverse.fibers.Suspendable;
import io.duna.test.utils.ClassFileExtraction;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static net.bytebuddy.dynamic.loading.ClassInjector.DEFAULT_PROTECTION_DOMAIN;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuspendableMethodTransformerTest {

    private static final String METHOD_NAME = "bar";

    private ClassLoader classLoader;

    @BeforeAll
    public static void setUp() {
        ByteBuddyAgent.install();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        classLoader = new ByteArrayClassLoader.ChildFirst(getClass().getClassLoader(),
            ClassFileExtraction.of(SimpleInterface.class,
                SimpleClass.class),
            DEFAULT_PROTECTION_DOMAIN,
            ByteArrayClassLoader.PersistenceHandler.MANIFEST,
            PackageDefinitionStrategy.NoOp.INSTANCE);
    }

    @Test
    public void testSuspendableMethodRebasing() throws Exception {
        assertNotNull(ByteBuddyAgent.install());

        ClassFileTransformer classFileTransformer = new AgentBuilder.Default()
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REBASE)
            .with(AgentBuilder.RedefinitionStrategy.DISABLED)
            .type(is(SimpleInterface.class), is(classLoader))
                .transform(new SuspendableMethodsTransformer())
            .installOnByteBuddyAgent();

        Class<?> iface = classLoader.loadClass(SimpleInterface.class.getName());
        Class<?> impl = classLoader.loadClass(SimpleClass.class.getName());

        Method m = impl.getMethod("foo");

        assertTrue(iface.getMethod(METHOD_NAME).isDefault());
        assertTrue(iface.getMethod(METHOD_NAME).isAnnotationPresent(Suspendable.class));
        assertEquals(m.invoke(impl.newInstance()), "Worked.");
    }

    public interface SimpleInterface {
        default String bar() {
            return "Worked.";
        }

        default String foo() {
            return bar();
        }

        void baz();
    }

    public static class SimpleClass implements SimpleInterface {
        @Override
        public void baz() {}
    }
}
