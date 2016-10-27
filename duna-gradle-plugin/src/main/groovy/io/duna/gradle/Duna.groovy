package io.duna.gradle

import io.duna.gradle.tasks.GenerateServiceActionHandlers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class Duna implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.extensions.create("duna", DunaOptionsExtension)
    project.tasks.create(
        name: "generateServiceActionHandlers",
        type: GenerateServiceActionHandlers,
        group: "code generation",
        description: "Generate the service action handlers"
    )

    if (!project.plugins.hasPlugin(JavaPlugin)) {
      project.apply plugin: JavaPlugin
    }

    if (project.plugins.hasPlugin("kotlin")) {
      project.tasks.getByName("compileKotlin").dependsOn("generateServiceActionHandlers")
    } else {
      project.tasks.getByName("compileJava").dependsOn("generateServiceActionHandlers")
    }
  }
}
