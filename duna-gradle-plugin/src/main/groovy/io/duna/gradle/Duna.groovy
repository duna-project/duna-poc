/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle

import io.duna.gradle.tasks.RunApplication
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class Duna implements Plugin<Project> {

  public static final String VERSION = "0.1-SNAPSHOT"

  @Override
  void apply(Project project) {
    project.extensions.create("duna", DunaOptionsExtension)

    if (!project.plugins.hasPlugin(JavaPlugin)) {
      project.apply plugin: JavaPlugin
    }

    project.classes {
      doFirst {
        ant.taskdef(name: 'scanSuspendables',
          classname: 'co.paralleluniverse.fibers.instrument.SuspendablesScanner',
          classpath: "build/classes/main:build/resources/main:${project.configurations.runtime.asPath}")
        ant.scanSuspendables(auto: false,
          suspendablesFile: "${project.sourceSets.main.output.resourcesDir}/META-INF/suspendables",
          supersFile: "${project.sourceSets.main.output.resourcesDir}/META-INF/suspendable-supers",
          append: true) {
          fileset(dir: project.sourceSets.main.output.classesDir)
        }
      }
    }

    RunApplication.createRunTask(project)
  }
}
