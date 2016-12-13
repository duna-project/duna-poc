package io.duna.core.external

import com.google.inject.BindingAnnotation

/**
 * Annotates extensions defining ports to the services cluster.
 *
 * @author [Carlos Eduardo Melo][cemelo@redime.com.br]
 */
@BindingAnnotation
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Port
