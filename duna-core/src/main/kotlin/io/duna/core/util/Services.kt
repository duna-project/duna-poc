package io.duna.core.util

import com.google.inject.BindingAnnotation
import io.duna.core.service.Service
import javax.inject.Qualifier

object Services {

  fun getQualifier(serviceClass: Class<*>): Annotation? =
      serviceClass.annotations
          .filter { it.javaClass.isAssignableFrom(Service::class.java) }
          .find {
            it.javaClass.isAnnotationPresent(Qualifier::class.java) ||
                it.javaClass.isAnnotationPresent(BindingAnnotation::class.java)
          }
}