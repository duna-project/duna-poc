/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.cluster;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.log4j2.Log4J2Logger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.net.URL;
import java.util.regex.Pattern;

public class IgniteClusterManagerFactory {

    public static ClusterManager create() {
        Config config = ConfigFactory.load();

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(config.getStringList("duna.cluster.hosts"));

        discoverySpi.setIpFinder(ipFinder);

        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress(config.getString("duna.cluster.local-address"));

        String portRange = config.getString("duna.cluster.local-port-range");

        if (Pattern.matches("[0-9]+[:-][0-9]+", portRange)) {
            int bottom = Integer.parseInt(portRange.split("[:-]")[0]);
            int top = Integer.parseInt(portRange.split("[:-]")[1]);

            int port = (top < bottom) ? top : bottom;
            int range = (top < bottom) ? bottom - top : top - bottom;

            communicationSpi.setLocalPort(port);
            communicationSpi.setLocalPortRange(range);
        } else if (Pattern.matches("[0-9]+", portRange)) {
            communicationSpi.setLocalPort(Integer.parseInt(portRange));
        }

        IgniteConfiguration igniteConfiguration = new IgniteConfiguration()
            .setDiscoverySpi(discoverySpi)
            .setCommunicationSpi(communicationSpi)
            .setGridLogger(null);

        URL loggerConfiguration = ClassLoader.getSystemClassLoader()
            .getResource("log4j2.xml");

        if (loggerConfiguration != null) {
            try {
                igniteConfiguration
                    .setGridLogger(new Log4J2Logger(loggerConfiguration));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new IgniteClusterManager(igniteConfiguration);
    }
}
