package io.duna.core.service

import com.google.inject.BindingAnnotation

/**
 * Identifies a concrete class as a service implementation.
 *
 * Different implementations for the same service must be annotated with
 * a [@Qualifier] in order to be identified at the injection points.
 */
@BindingAnnotation
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service