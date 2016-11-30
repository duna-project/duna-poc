import io.duna.core.service.Service

@Service
class ServiceAImpl : ServiceA {
  override fun ping(request: String): String {
    return "pong"
  }
}