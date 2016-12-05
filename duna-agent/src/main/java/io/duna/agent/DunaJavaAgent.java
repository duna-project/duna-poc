package io.duna.agent;

import io.duna.core.service.Contract;
import io.duna.instrument.AgentInstrumentationListener;
import io.duna.instrument.SuspendableMethodsTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;

/**
 * The Duna java agent.
 *
 * @author <a href="mailto:ceduardo.melo@gmail.com">Carlos Eduardo Melo</a>
 */
public class DunaJavaAgent {

    private static final DunaJavaAgent INSTANCE = new DunaJavaAgent();

    private static final Logger LOGGER = LogManager.getLogManager()
        .getLogger(DunaJavaAgent.class.getName());

    private ClassFileTransformer classFileTransformer;

    private Instrumentation instrumentation;

    public static void premain(String args, Instrumentation instrumentation) {
        LOGGER.fine("Attaching the Duna JavaAgent at bootstrap");
        INSTANCE.install(args, instrumentation);
    }

    private void install(String args, Instrumentation instrumentation) {
        classFileTransformer = new AgentBuilder.Default()
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
            .with(new AgentInstrumentationListener())
            .type(isAnnotatedWith(Contract.class).and(isInterface()))
            .transform(new SuspendableMethodsTransformer())
            .installOn(instrumentation);

        this.instrumentation = instrumentation;
    }

    public Instrumentation getInstrumentation() {
        if (instrumentation == null)
            throw new UnsupportedOperationException("Agent not installed.");

        return instrumentation;
    }

    public ClassFileTransformer getClassFileTransformer() {
        if (classFileTransformer == null)
            throw new UnsupportedOperationException("Agent not installed.");

        return classFileTransformer;
    }

    public boolean isAttached() {
        return instrumentation != null;
    }
}
