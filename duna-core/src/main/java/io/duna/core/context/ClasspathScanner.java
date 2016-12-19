/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.context;

import io.duna.core.service.Contract;
import io.duna.port.Port;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClasspathScanner {

    private List<String> portExtensions;

    private List<String> allContracts;

    private List<String> localServices;

    private List<String> remoteServices;

    private Map<String, Set<ClassInfo>> implementationsList;

    public ClasspathScanner() {
        ExecutorService executorService = Executors
            .newFixedThreadPool(6);

        ScanResult result = new FastClasspathScanner()
            .enableFieldTypeIndexing(false)
            .enableMethodAnnotationIndexing(false)
            .ignoreFieldVisibility(false)
            .ignoreMethodVisibility(false)
            .scan(executorService, 6);

        portExtensions = result.getNamesOfClassesWithAnnotation(Port.class);
        allContracts = result.getNamesOfClassesWithAnnotation(Contract.class);

        localServices = new ArrayList<>();
        remoteServices = new ArrayList<>();
        implementationsList = new ConcurrentHashMap<>();

        allContracts
            .parallelStream()
            .forEach(contract -> {
                Set<ClassInfo> implementations = result.getClassNameToClassInfo()
                    .get(contract)
                    .getClassesImplementing();

                if (implementations.isEmpty()) {
                    remoteServices.add(contract);
                } else {
                    localServices.add(contract);
                    implementationsList.put(
                        contract,
                        implementations
                    );
                }
            });

        executorService.shutdown();
    }

    public List<String> getPortExtensions() {
        return portExtensions;
    }

    public List<String> getAllContracts() {
        return allContracts;
    }

    public List<String> getLocalServices() {
        return localServices;
    }

    public List<String> getRemoteServices() {
        return remoteServices;
    }

    public Map<String, Set<ClassInfo>> getImplementationsList() {
        return implementationsList;
    }
}
