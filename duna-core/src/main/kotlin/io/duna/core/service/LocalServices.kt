package io.duna.core.service

import javax.inject.Qualifier

/**
 * Annotates an injection point where the local service instances will be injected.
 */
@Qualifier
@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalServices
