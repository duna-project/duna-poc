/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.cluster;

import io.duna.bootstrap.SupervisorVerticle;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.channel.DefaultChannelId;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusteredMain {

    public static void main(String ... args) {
        Logger rootLogger = LogManager.getRootLogger();
        rootLogger.info(() -> "Starting the cluster node");

        Config config = ConfigFactory.load();
        ClusterManager clusterManager = HazelcastClusterManagerFactory.create();

        // Load default netty channel to prevent the BlockedThreadChecker exception
        DefaultChannelId.newInstance();

        VertxOptions vertxOptions = new VertxOptions()
            .setClustered(true)
            .setClusterManager(clusterManager)
            .setHAEnabled(false)
            .setWorkerPoolSize(config.getInt("duna.thread-pool-size"));

        Vertx.clusteredVertx(vertxOptions, result -> {
            if (result.failed()) {
                rootLogger.error(
                    () -> "Error while creating clustered vert.x instance",
                    result.cause());
                return;
            }

            rootLogger.debug(() -> "Deploying the verticle supervisor");

            DeploymentOptions supervisorOptions = new DeploymentOptions()
                .setHa(false)
                .setInstances(1)
                .setMultiThreaded(false)
                .setWorker(true);

            result.result().deployVerticle(new SupervisorVerticle(), supervisorOptions);
        });
    }
}
