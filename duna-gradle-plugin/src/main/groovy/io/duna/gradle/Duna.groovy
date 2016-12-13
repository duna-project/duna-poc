/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle

import io.duna.gradle.tasks.TransformContractClasses
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec

class Duna implements Plugin<Project> {

  public static final String VERSION = "0.1.0-SNAPSHOT"

  @Override
  void apply(Project project) {
    project.extensions.create("duna", DunaOptionsExtension)

    if (!project.plugins.hasPlugin(JavaPlugin)) {
      project.apply plugin: JavaPlugin
    }

    createRunTask(project)
    createTransformTask(project)

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
  }

  private void createRunTask(Project project) {
    project.afterEvaluate {
      project.configurations {
        agent
      }

      if (project.ext.hasProperty("versions") &&
        project.ext.versions.contains("duna")) {
        project.dependencies {
          agent "io.duna:duna-agent:${project.ext.versions.duna}"
        }
      } else {
        project.dependencies {
          agent "io.duna:duna-agent:${VERSION}"
        }
      }

      project.tasks.create(
        name: 'run',
        type: JavaExec,
        group: 'application',
        description: 'Run the service as a standalone application',
        dependsOn: project.build
      ) {
        classpath = project.sourceSets.main.runtimeClasspath
        main = 'io.duna.core.Main'

        jvmArgs "-javaagent:${project.configurations.agent.find {it.name.contains("duna-agent")}}",
          "-javaagent:${project.configurations.compile.find {it.name.contains("quasar-core")}}",
          "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
      }
    }
  }

  private void createTransformTask(Project project) {
    project.tasks.create(
      name: 'transformContractClasses',
      type: TransformContractClasses,
      group: 'build',
      dependsOn: project.compileJava
    ) {
      sourceFiles = project.sourceSets.main.allSource.files
      classesDir = project.sourceSets.main.output.classesDir
      outputDir = project.sourceSets.main.output.classesDir
    }

    project.tasks.classes.dependsOn(project.tasks.transformContractClasses)
  }

  private void createScanSuspendablesTask(Project project) {

  }
}
