/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.cluster;

import com.hazelcast.config.JoinConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.regex.Pattern;

public class HazelcastClusterManagerFactory {
    public static ClusterManager create() {
        Config dunaConfig = ConfigFactory.load();

        com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
        hazelcastConfig.setClassLoader(Thread.currentThread().getContextClassLoader());

        String portRange = dunaConfig.getString("duna.cluster.local-port-range");

        if (Pattern.matches("[0-9]+[:-][0-9]+", portRange)) {
            int bottom = Integer.parseInt(portRange.split("[:-]")[0]);
            int top = Integer.parseInt(portRange.split("[:-]")[1]);

            int port = (top < bottom) ? top : bottom;
            int range = (top < bottom) ? bottom - top : top - bottom;

            hazelcastConfig.getNetworkConfig()
                .setPort(port)
                .setPortAutoIncrement(true)
                .setPortCount(range);
        } else if (Pattern.matches("[0-9]+", portRange)) {
            hazelcastConfig.getNetworkConfig()
                .setPort(Integer.parseInt(portRange))
                .setPortAutoIncrement(false);
        }

        if (dunaConfig.hasPath("duna.cluster.local-address")) {
            hazelcastConfig.getNetworkConfig()
                .getInterfaces()
                .setEnabled(true)
                .setInterfaces(dunaConfig.getStringList("duna.cluster.local-addresses"));
        }

        JoinConfig joinConfig = hazelcastConfig.getNetworkConfig().getJoin();

        dunaConfig.getStringList("duna.cluster.hosts")
            .forEach(joinConfig.getTcpIpConfig()::addMember);

        return new HazelcastClusterManager(hazelcastConfig);
    }
}
