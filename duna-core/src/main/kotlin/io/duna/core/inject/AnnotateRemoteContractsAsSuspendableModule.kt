package io.duna.core.inject

import co.paralleluniverse.fibers.Suspendable
import com.google.inject.AbstractModule
import io.duna.core.SupervisorVerticle
import io.duna.core.classpath.ClasspathScanner
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.pool.TypePool

/**
 * Annotates every contract interface method as suspendable.
 */
internal class AnnotateRemoteContractsAsSuspendableModule : AbstractModule() {

  override fun configure() {
    ClasspathScanner.getRemoteServices().forEach { contract ->
      val baseInterface = TypePool.Default.ofClassPath()
        .describe(contract)
        .resolve()

      // @formatter:off
      ByteBuddy()
        .redefine <Any>(baseInterface, ClassFileLocator.ForClassLoader.ofClassPath())
        .method(ElementMatchers.isDeclaredBy(baseInterface))
          .withoutCode()
          .annotateMethod(
            AnnotationDescription.Builder
              .ofType(Suspendable::class.java).build())
        .make()
        .load(SupervisorVerticle::class.java.classLoader)
      // @formatter:on
    }
  }
}