/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.context;

import io.duna.core.service.Contract;
import io.duna.core.service.Service;
import io.duna.extend.Port;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClasspathScanner {

    private List<String> portExtensions;

    private List<String> allContracts;

    private Set<String> localServices;

    private Set<String> remoteServices;

    private Map<String, List<ClassInfo>> implementationsList;

    public ClasspathScanner() {
        ExecutorService executorService = Executors
            .newFixedThreadPool(2);

        Config config = ConfigFactory.load();

        String[] scanSpec =
            Stream.concat(
                config.getStringList("duna.classpath.scan-packages").stream(),
                config.getStringList("duna.classpath.ignore-packages")
                    .stream()
                    .map(p -> "-" + p))
            .toArray(String[]::new);

        ScanResult result = new FastClasspathScanner(scanSpec)
            .enableFieldTypeIndexing(false)
            .enableMethodAnnotationIndexing(false)
            .ignoreFieldVisibility(false)
            .ignoreMethodVisibility(false)
            .scan(executorService, 2);

        portExtensions = result.getNamesOfClassesWithAnnotation(Port.class);
        allContracts = result.getNamesOfClassesWithAnnotation(Contract.class);

        implementationsList = new ConcurrentHashMap<>();

        remoteServices = allContracts
            .parallelStream()
            .filter(contract -> result.getClassNameToClassInfo()
                .get(contract)
                .getClassesImplementing()
                .stream()
                .noneMatch(ci -> ci.hasDirectAnnotation(Service.class.getName())))
            .collect(Collectors.toCollection(HashSet::new));

        localServices = allContracts
            .parallelStream()
            .filter(contract -> result.getClassNameToClassInfo()
                .get(contract)
                .getClassesImplementing()
                .stream()
                .anyMatch(ci -> ci.hasDirectAnnotation(Service.class.getName())))
            .collect(Collectors.toCollection(HashSet::new));

        implementationsList = localServices
            .parallelStream()
            .collect(Collectors.toConcurrentMap(lc -> lc, lc -> result.getClassNameToClassInfo()
                .get(lc)
                .getClassesImplementing()
                .stream()
                .filter(ci -> ci.hasDirectAnnotation(Service.class.getName()))
                .collect(Collectors.toList())
            ));

        executorService.shutdown();
    }

    public List<String> getPortExtensions() {
        return portExtensions;
    }

    public List<String> getAllContracts() {
        return allContracts;
    }

    public Set<String> getLocalServices() {
        return localServices;
    }

    public Set<String> getRemoteServices() {
        return remoteServices;
    }

    public Map<String, List<ClassInfo>> getImplementationsList() {
        return implementationsList;
    }
}
