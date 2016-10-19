package io.duna.core.test.proxy

import io.duna.core.proxy.ProxyClassLoader
import io.duna.core.proxy.ServiceProxyFactory
import io.duna.core.service.Contract

//@RunWith(VertxUnitRunner::class)
//class ProxyClassLoaderTest {
//
//  @get:Rule
//  var rule = RunTestOnContext()
//
//  val classLoader = ProxyClassLoader(javaClass.classLoader, ServiceProxyFactory())
//
//  @Test
//  fun testServiceProxy(context: TestContext) {
//    val vertx = rule.vertx()
//
//    vertx.eventBus().consumer<String>("testEchoService", { msg ->
//      msg.reply(msg.body())
//    })
//
//    val proxyClass = classLoader.proxyForService(EchoService::class.java)
//
//    println(proxyClass.constructors)
//
////    val echoService = proxyClass.constructors[0].newInstance("testEchoService") as EchoService
////
////    val result = echoService.echo("testMessage")
////    context.assertEquals(result, "testMessage")
//  }
//}
//
//@Contract
//interface EchoService {
//  fun echo(message: String): String
//}