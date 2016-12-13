package io.duna.web;

import io.duna.core.service.LocalServices;
import io.duna.core.external.Port;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import javax.inject.Inject;
import java.util.*;

@Port
public class HttpRouterVerticle extends AbstractVerticle {

    private final Map<Class<?>, Object> localServices;

    @Inject
    public HttpRouterVerticle(@LocalServices Map<Class<?>, Object> localServices) {
        this.localServices = localServices;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Set<Class<?>> contracts = new HashSet<>();

//        localServices
//            .parallelStream()
//            .flatMap(s -> Arrays.stream(s.getClass().getInterfaces()))
//            .filter(c -> c.isAnnotationPresent(Contract.class))
//            .forEach(System.out::println);

        System.out.println(localServices);
//        Arrays.asList(this.getClass().getInterfaces());

        startFuture.complete();
    }
}
