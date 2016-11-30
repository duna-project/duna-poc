package io.duna.core.service

import com.google.inject.BindingAnnotation

@BindingAnnotation
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class AllServices