package io.duna.web;

import io.duna.core.service.LocalServices;
import io.duna.core.vertx.BridgeVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import javax.inject.Inject;
import java.util.Set;

@BridgeVerticle
public class HttpRouterVerticle extends AbstractVerticle {

    private final Set<Class<?>> localContracts;

    @Inject
    public HttpRouterVerticle(@LocalServices Set<Class<?>> localContracts) {
        this.localContracts = localContracts;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        localContracts.forEach(System.out::println);
        startFuture.complete();
    }
}
