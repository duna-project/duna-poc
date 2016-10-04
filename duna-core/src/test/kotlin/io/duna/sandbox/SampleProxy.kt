package io.duna.sandbox

/**
 * Created by eduribeiro on 04/10/2016.
 */
class SampleProxy : SampleService {

  override fun call(m: String, n: POJO, z: Int): POJO {
    // Call handler
    return POJO()
  }
}