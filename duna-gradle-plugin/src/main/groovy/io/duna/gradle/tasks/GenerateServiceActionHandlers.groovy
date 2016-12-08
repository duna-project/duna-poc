/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskAction

class GenerateServiceActionHandlers extends DefaultTask {

  GenerateServiceActionHandlers() {
    if (project.plugins.hasPlugin("kotlin")) {
      project.tasks.getByName("compileKotlin").dependsOn("generateServiceActionHandlers")
    } else if (project.plugins.hasPlugin(JavaPlugin)) {
      project.tasks.getByName("compileJava").dependsOn("generateServiceActionHandlers")
    }
  }

  @TaskAction
  def generateServiceActionHandlers() {

  }
}
