package io.duna.core.service

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalServices
