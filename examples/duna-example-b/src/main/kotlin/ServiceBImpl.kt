import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.Strand
import com.google.inject.Inject
import io.duna.core.service.Service

@Service
class ServiceBImpl : ServiceB {

  @Inject
  lateinit var serviceA: ServiceA

  @Suspendable
  override fun forwardPing(): String {
    println("Sleeping for a second")
    Strand.sleep(1000)
    println("requesting")
    return serviceA.ping("request")
  }
}