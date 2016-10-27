package io.duna.sandbox

import java.util.List

interface SampleService {
  fun call(m: String?, n: POJO?, z: Double, x: Boolean, t: java.util.List<Any>?): POJO?
}

data class POJO(var id: Int? = null,
                var text: String? = null) {
}

object SampleServiceImpl : SampleService {
  override fun call(m: String?, n: POJO?, z: Double, x: Boolean, t: List<Any>?): POJO? {
    val p = POJO()
    p.id = 0
    p.text = "asd"

    return p
  }


}