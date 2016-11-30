import co.paralleluniverse.fibers.Suspendable
import com.google.inject.Inject
import io.duna.core.service.Service

@Service
class ServiceBImpl : ServiceB {

  @Inject
  lateinit var serviceA: ServiceA

  @Suspendable
  override fun forwardPing(): String {
    println("requesting")
    return serviceA.ping("request")
  }
}