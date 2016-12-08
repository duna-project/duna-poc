package io.duna.gradle.tasks

import io.duna.gradle.Duna
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class RunApplication {

  static void createRunTask(Project project) {

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
          agent "io.duna:duna-agent:${Duna.VERSION}"
        }
      }

      project.tasks.create(
        name: 'run',
        type: JavaExec,
        group: 'application',
        description: 'Run the microservice application',
        dependsOn: project.classes
      ) {
        classpath = project.sourceSets.main.runtimeClasspath
        main = 'io.duna.core.Main'

        jvmArgs "-javaagent:${project.configurations.agent.find {it.name.contains("duna-agent")}}",
          "-javaagent:${project.configurations.compile.find {it.name.contains("quasar-core")}}",
          "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
      }
    }
  }
}
