package io.duna.instrument;

import co.paralleluniverse.fibers.Suspendable;
import io.duna.test.utils.ClassFileExtraction;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import static net.bytebuddy.dynamic.loading.ClassInjector.DEFAULT_PROTECTION_DOMAIN;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.none;
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
        assertTrue(ByteBuddyAgent.install() instanceof Instrumentation);

        ClassFileTransformer classFileTransformer = new AgentBuilder.Default()
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REBASE)
            .with(AgentBuilder.RedefinitionStrategy.DISABLED)
            .with(new AgentBuilder.Listener() {

                @Override
                public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
                    System.out.println(typeDescription + " is instrumented");
                }

                @Override
                public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {

                }

                @Override
                public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {

                }
            })
            .type(is(SimpleInterface.class), is(classLoader))
                .transform(new SuspendableMethodsTransformer())
            .installOnByteBuddyAgent();

        Class<?> iface = classLoader.loadClass(SimpleInterface.class.getName());

        System.out.println(iface.getMethod(METHOD_NAME).isDefault());

//        Class<?> clazz = classLoader.loadClass(SimpleClass.class.getName());
//
//        Object instance = clazz.newInstance();
//        iface.getMethod(METHOD_NAME).invoke(instance);

        System.out.println(iface.getMethod(METHOD_NAME).isAnnotationPresent(Suspendable.class));
    }

    public interface SimpleInterface {
        default void bar() {
            System.out.println("Works");
        }

        default void foo() {
            bar();
        }
    }

    public static class SimpleClass implements SimpleInterface {
    }
}
