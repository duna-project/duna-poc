package io.duna.core.service.invocation;

import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;

import java.lang.reflect.Method;

/**
 *
 */
public class ProxyCallImplementation implements Implementation {

    private Method target;

    public ProxyCallImplementation(Method target) {
        this.target = target;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return new ProxyCallInterceptorMethod(target);
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}
