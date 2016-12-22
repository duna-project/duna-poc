/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core;

import io.duna.core.bootstrap.SupervisorVerticle;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.logging.Logger;

import static org.apache.logging.log4j.jul.LogManager.getLogManager;

public class Main {

    public static void main(String ... args) {
        Config config = ConfigFactory.load();

        VertxOptions vertxOptions = new VertxOptions()
            .setHAEnabled(false)
            .setClustered(false)
            .setEventLoopPoolSize(1)
            .setWorkerPoolSize(config.getInt("duna.thread-pool-size"));

        Logger rootLogger = getLogManager()
            .getLogger("Duna");

        rootLogger.warning(() -> "This execution mode should only be deployed in development. " +
            "It doesn't take advantage of the parallel capabilities of Vert.x.");

        rootLogger.info(() -> "Starting standalone duna node");

        Vertx vertx = Vertx.vertx(vertxOptions);

        rootLogger.fine(() -> "Deploying the supervisor");
        vertx.deployVerticle(new SupervisorVerticle());
    }
}
