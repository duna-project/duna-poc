/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle.tasks;

import io.duna.http.annotations.HttpService;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import spoon.Launcher;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ParseHttpServicesMetadata extends DefaultTask {

    @SkipWhenEmpty
    @InputFiles
    public Set<File> sourceFiles;

    @OutputDirectory
    public File outputDir;

    Map<String, List<String>> methodParameterNames = new ConcurrentHashMap<>();

    @TaskAction
    public void parseMetadata() {
        Launcher launcher = new Launcher();

        inputs.outOfDate(changed -> launcher.addInputResource(changed.getFile().getAbsolutePath()));
        launcher.run();

        Factory factory = launcher.getFactory();

        factory.Class()
            .getAll(true)
            .parallelStream()
            .forEach(type -> {
//                if (!isHttpService(type)) return;

                type.getAllMethods()
                    .forEach(method -> {
                        System.out.println(method.getSignature());
                    });
            });


    }

    private boolean isHttpService(CtType<?> aType) {
        boolean hasAnnotation = false;

        for (CtAnnotation<?> annotation : aType.getAnnotations()) {
            if (annotation.getAnnotationType().getQualifiedName().equals(HttpService.class.getName()))
                hasAnnotation = true;
        }

        return hasAnnotation;
    }
}
