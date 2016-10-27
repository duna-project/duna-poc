package io.duna.vertx

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.FiberForkJoinScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import co.paralleluniverse.strands.Strand
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import java.util.concurrent.atomic.AtomicInteger

object Fi {
  @JvmStatic
  val atomic = AtomicInteger(1)

  @JvmStatic
  val local = object : ThreadLocal<Int>() {

    override fun initialValue(): Int {
      return (Math.random() * 50).toInt()
    }
  }
}

object Test {

  @Suspendable
  fun x() {
    val ctx = Fi.local.get()

    println("A: $ctx ${Fiber.currentStrand().id}")

    Fi.local.set(ctx + 1)

    Strand.sleep(1000)

    println("B: $ctx ${Fiber.currentStrand().id}")
  }

  @JvmStatic
  fun main(vararg args: String) {
    val vertxOptions = VertxOptions()
      .setEventLoopPoolSize(1)

    val vertx = Vertx.vertx(vertxOptions)
    vertx.deployVerticle(MainVerticle())
    vertx.deployVerticle(OtherVerticle())

//    fiber { x() }
//    fiber { x() }
//    fiber { x() }
//    fiber { x() }
//    fiber { x() }
  }
}