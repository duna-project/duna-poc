package io.duna.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.model.Mutate
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode

class TransformContractClasses extends DefaultTask {

  @InputFiles
  Set<File> sourceFiles

  @Input
  File classesDir

  @OutputDirectory
  File outputDir

  @TaskAction
  @Mutate
  def transform(IncrementalTaskInputs inputs) {
    println inputs.incremental ?
      "CHANGED inputs considered out of date" :
      "ALL inputs considered out of date"

    println sourceFiles

    inputs.outOfDate { changed ->
      println "Transforming ${changed.file.name}"
      transformClassFile(changed.file)
    }
  }

  def transformClassFile(File file) {
//    ClassNode classNode = new ClassNode()
//
//    ClassReader classReader = new ClassReader(new FileInputStream(file))
//    classReader.accept(classNode, ClassReader.SKIP_CODE)
  }
}
