import io.duna.core.service.invocation.CallableServiceProxy;
import io.duna.core.service.invocation.ProxyCallImplementation;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Test {
    public static void main(String ... args) throws Exception {
        Method m = X.class.getMethod("ping", String.class);

        DynamicType.Unloaded bb = new ByteBuddy(ClassFileVersion.JAVA_V8)
                .subclass(Object.class)
                .implement(CallableServiceProxy.class)
                .method(named("invoke"))
                .intercept(new ProxyCallImplementation(m))
                .make();

        bb.saveIn(new File("/Users/carlos/ByteBuddy"));

        Class<?> proxy = bb
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded();
//
        CallableServiceProxy csp = (CallableServiceProxy) proxy.newInstance();
        X svc = new X();

        String result = csp.invoke(svc, "asd");

        System.out.println(result);
    }

    public void x() {
    }

    public Object y() {
        x();
        return null;
    }
}
