import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.vertx.core.Verticle
import java.lang.reflect.Modifier

object Test {

  @JvmStatic
  fun main(args: Array<String>) {
    val result = FastClasspathScanner()
        .matchClassesImplementing(Verticle::class.java, {
          if (it?.`package`?.name?.startsWith("io.vertx") ?: true)
            return@matchClassesImplementing

          if (Modifier.isAbstract(it?.modifiers ?: 0))
            return@matchClassesImplementing

          println(it)
        })
      .scan()
  }
}