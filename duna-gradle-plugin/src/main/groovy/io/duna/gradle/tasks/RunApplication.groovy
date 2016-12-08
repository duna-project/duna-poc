package io.duna.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class RunApplication {

  static void createRunTask(Project project) {

    project.configurations {
      agent
    }

    project.dependencies {
      agent "io.duna:duna-agent:${VERSION}"
    }

    project.tasks.create(
      name: 'run',
      type: JavaExec,
      group: 'application',
      description: 'Run the microservice application',
      dependsOn: project.classes
    ) {
      classpath = sourceSets.main.runtimeClasspath
      main = 'io.duna.core.Main'

      jvmArgs "-javaagent:${project.configurations.agent.singleFile}",
        "-javaagent:${project.configurations.compile.find {it.name.contains("quasar-core")}}",
        "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
    }
  }
}
