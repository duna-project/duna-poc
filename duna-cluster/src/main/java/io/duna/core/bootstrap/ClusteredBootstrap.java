/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.bootstrap;

import io.duna.core.cluster.IgniteClusterManagerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusteredBootstrap {

    public static void main(String ... args) {
        Config config = ConfigFactory.load();
        ClusterManager clusterManager = IgniteClusterManagerFactory.create();

        VertxOptions vertxOptions = new VertxOptions()
            .setClustered(true)
            .setClusterManager(clusterManager)
            .setHAEnabled(config.getBoolean("duna.vertx.ha-enabled"))
            .setWorkerPoolSize(config.getInt("duna.thread-pool-size"));

        Logger rootLogger = LogManager.getRootLogger();

        rootLogger.info(() -> "Starting the cluster node");

        Vertx.clusteredVertx(vertxOptions, result -> {
            if (result.failed()) {
                rootLogger.error(
                    () -> "Error while creating clustered vert.x instance",
                    result.cause());
                return;
            }

            rootLogger.debug(() -> "Deploying the verticle supervisor");
            result.result().deployVerticle(new SupervisorVerticle());
        });
    }
}
