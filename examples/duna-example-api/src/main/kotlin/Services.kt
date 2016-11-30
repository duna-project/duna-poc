import io.duna.core.service.Contract

@Contract
interface ServiceA {
  fun ping(request: String): String
}

@Contract
interface ServiceB {
  fun forwardPing(): String
}