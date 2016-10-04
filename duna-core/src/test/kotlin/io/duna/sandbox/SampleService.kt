package io.duna.sandbox

interface SampleService {
  fun call(m: String, n: POJO, z: Int): POJO
}

class POJO {
  var id: Int? = null
  var text: String? = null
}
