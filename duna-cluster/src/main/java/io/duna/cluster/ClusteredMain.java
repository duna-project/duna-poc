/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.cluster;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.duna.bootstrap.SupervisorVerticle;
import io.netty.channel.DefaultChannelId;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClusteredMain {

    public static void main(String ... args) {
        Logger rootLogger = Logger.getAnonymousLogger();
        rootLogger.info("Starting the cluster node");

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
                rootLogger.log(Level.SEVERE, result.cause(),
                    () -> "Error while creating clustered vert.x instance");
                return;
            }

            rootLogger.fine("Deploying the verticle supervisor");

            DeploymentOptions supervisorOptions = new DeploymentOptions()
                .setHa(false)
                .setInstances(1)
                .setMultiThreaded(false)
                .setWorker(true);

            result.result().deployVerticle(new SupervisorVerticle(), supervisorOptions);
        });
    }
}
