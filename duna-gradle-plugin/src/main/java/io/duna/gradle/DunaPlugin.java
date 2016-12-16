/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle;

import io.duna.gradle.tasks.ParseServiceSourceMetadataTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;

public class DunaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        project.getConvention().getPlugin(JavaPluginConvention.class)
            .getSourceSets()
            .all(sourceSet -> {
                // 1) Create the parsing task for this source set
                String parseMetadataTaskName = sourceSet.getTaskName("parse", "ServiceSourceMetadata");
                ParseServiceSourceMetadataTask parseMetadataTask = project.getTasks()
                    .create(parseMetadataTaskName, ParseServiceSourceMetadataTask.class);
                parseMetadataTask.setDescription("Processes the " + sourceSet.getName() +
                    " sources in order to extract service metadata.");

                // 2) Set the java files as input
                parseMetadataTask.setSource(sourceSet.getAllJava());

                // 3) Set up the output file
                parseMetadataTask.setOutputFile(sourceSet.getOutput().getResourcesDir()
                    .toPath()
                    .resolve("META-INF")
                    .resolve("services-metadata")
                    .toFile());

                // 4) Register this task before compilation
                project.getTasks()
                    .getByName(sourceSet.getCompileJavaTaskName())
                    .dependsOn(parseMetadataTaskName);
            });
    }
}
