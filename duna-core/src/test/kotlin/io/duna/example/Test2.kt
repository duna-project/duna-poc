package io.duna.example

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Named
import com.google.inject.spi.BindingTargetVisitor
import com.google.inject.spi.DefaultBindingTargetVisitor
import com.google.inject.spi.LinkedKeyBinding

@Retention(AnnotationRetention.RUNTIME)
@BindingAnnotation
annotation class Red

@Retention(AnnotationRetention.RUNTIME)
@BindingAnnotation
annotation class Blue

interface IA
class A : IA

@Red
class B : IA

@Blue
class C : IA

/**
 * Created by carlos on 28/11/16.
 */
fun main(vararg args: String) {
  println(Any::class.javaObjectType)
//  val injector = Guice.createInjector(object : AbstractModule() {
//    override fun configure() {
//      bind(IA::class.java).goTo(A::class.java)
//      bind(IA::class.java).annotatedWith(Red::class.java).goTo(B::class.java)
//      bind(IA::class.java).annotatedWith(Blue::class.java).goTo(C::class.java)
//    }
//  })
//
//  injector.bindings.entries
//    .filter { it.key.typeLiteral.rawType.isAssignableFrom(IA::class.java) }
//    .forEach { println(it.value); it.value.acceptTargetVisitor(object : DefaultBindingTargetVisitor<Any, Unit>() {
//      override fun visit(linkedKeyBinding: LinkedKeyBinding<out Any>?): Unit {
//        println(linkedKeyBinding?.linkedKey)
//      }
//    }) }
}

class Test {

}