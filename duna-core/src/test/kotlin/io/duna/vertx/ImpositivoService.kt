package io.duna.vertx

import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.strands.Strand
import io.duna.core.service.Contract
import io.duna.core.service.Service
import io.vertx.core.Future
import java.math.BigDecimal

@Contract
interface ImpositivoService {
  fun obterValores(filtro: Classificacao): ValorImpositivo
}

@Service
class ImpositivoServiceImpl : ImpositivoService {

  @Suspendable
  override fun obterValores(filtro: Classificacao): ValorImpositivo {
    Strand.sleep(1000)
    return ValorImpositivo(filtro, BigDecimal("10.1"), emptyArray())
  }
}

data class Classificacao(val exercicio: Int?,
                    val autor: Autor?,
                    val emenda: Emenda?,
                    val programacao: Programacao?,
                    val dotacao: Dotacao?,
                    val beneficiario: Beneficiario?,
                    val impedimento: Impedimento?) {
  constructor() : this(null, null, null, null, null, null, null)
}

data class ValorImpositivo(val classificacao: Classificacao?,
                      val valor: BigDecimal?,
                      val detalhamento: Array<ValorImpositivo>?) {
  constructor() : this(null, null, null)
}

data class Impedimento(val id: Int?) {
  constructor() : this(null)
}

data class Beneficiario(val id: Int?) {
  constructor() : this(null)
}

data class Dotacao(val id: Int?) {
  constructor() : this(null)
}

data class Programacao(val id: Int?) {
  constructor() : this(null)
}

data class Emenda(val id: Int?) {
  constructor() : this(null)
}

data class Autor(val id: Int?) {
  constructor() : this(null)
}
