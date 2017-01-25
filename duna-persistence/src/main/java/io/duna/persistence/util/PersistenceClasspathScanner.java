/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.persistence.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class PersistenceClasspathScanner {

    private List<String> jpaEntities;

    public PersistenceClasspathScanner() {
        Config config = ConfigFactory.load();

        ScanResult result;

        List<String> packagesToScan = config.getStringList("duna.classpath.scan-packages");
        packagesToScan.add("io.duna");

        String[] scanSpec =
            Stream.concat(
                packagesToScan.stream(),
                config.getStringList("duna.classpath.ignore-packages")
                    .stream()
                    .map(p -> "-" + p))
                .toArray(String[]::new);

        boolean parallelScanning = config.getBoolean("duna.classpath.parallel");
        if (parallelScanning) {
            ExecutorService executorService = Executors
                .newFixedThreadPool(2);

            result = new FastClasspathScanner(scanSpec)
                .enableFieldTypeIndexing(false)
                .enableMethodAnnotationIndexing(false)
                .ignoreFieldVisibility(false)
                .ignoreMethodVisibility(false)
                .scan(executorService, 2);

            executorService.shutdown();
        } else {
            result = new FastClasspathScanner(scanSpec)
                .enableFieldTypeIndexing(false)
                .enableMethodAnnotationIndexing(false)
                .ignoreFieldVisibility(false)
                .ignoreMethodVisibility(false)
                .scan();
        }

        jpaEntities = result.getNamesOfClassesWithAnnotationsAnyOf(Entity.class, MappedSuperclass.class);
    }

    public List<String> getJpaEntities() {
        return jpaEntities;
    }
}
