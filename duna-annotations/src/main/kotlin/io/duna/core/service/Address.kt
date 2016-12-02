package io.duna.core.service

/**
 * The service address inside the cluster.
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Address(val value: String)