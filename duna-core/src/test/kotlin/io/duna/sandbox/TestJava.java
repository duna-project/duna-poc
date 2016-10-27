package io.duna.sandbox;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.MethodArguments;
import net.bytebuddy.description.modifier.ModifierContributor;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.auxiliary.MethodCallProxy;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Created by carlos on 27/10/16.
 */
public class TestJava {
    public static void main(String[] args) throws Exception {
        Class[] paramsTypes = new Class[]{String.class, int.class, POJO.class};
        Object[] params = new Object[]{"ASd", 1, new POJO(1, "ASd")};

        Invocation proxy = (Invocation) new ByteBuddy()
                .subclass(Object.class)
                .implement(Invocation.class)
                .defineMethod("invoke", void.class,
                        Visibility.PUBLIC,
                        MethodArguments.VARARGS)
                .intercept(
                        MethodCall.invoke(
                                Target.class.getDeclaredMethod("targetMethod", paramsTypes))
                                .on(Target.INSTANCE)
                                .with(params)
                ).make()
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded()
                .newInstance();

//        new MethodCallProxy(new Implementation.SpecialMethodInvocation.Simple()
//        )

        proxy.invoke(params);
    }
}
