package io.duna.core.service

import com.google.inject.BindingAnnotation

/**
 * Identifies an interface as a service contract.
 */
@BindingAnnotation
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Contract