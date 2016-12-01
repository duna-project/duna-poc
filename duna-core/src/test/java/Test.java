import io.duna.core.service.invocation.ServiceCallDelegation;
import io.duna.core.service.invocation.MethodCallDemuxing;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Test {
    public static void main(String ... args) throws Exception {
        Method m = X.class.getMethod("ping", int.class);

        X svc = new X();

        DynamicType.Unloaded bb = new ByteBuddy(ClassFileVersion.JAVA_V8)
            .subclass(Object.class)
            .name("MyProxy")
            .defineField("method", Method.class)
            .implement(ServiceCallDelegation.class)
            .intercept(FieldAccessor.ofField("method"))
            .method(named("invoke"))
            .intercept(new MethodCallDemuxing(m))
            .make();

        byte[] classBytes = bb.getBytes();

//        System.out.println(bb.getLoadedTypeInitializers());

//        bb.saveIn(new File("/Users/carlos/ByteBuddy"));

        Class<?> proxy = bb
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded();
//
        ServiceCallDelegation csp = (ServiceCallDelegation) proxy.newInstance();
        csp.setMethod(m);

        String result = csp.invoke(svc, "String");

        System.out.println(result);
    }

    public void x() {
    }

    public Object y() {
        x();
        return null;
    }
}
