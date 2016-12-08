/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service

/**
 * Identifies a concrete class as a service implementation.
 *
 * Different implementations for the same service must be annotated with
 * a [@Qualifier] in order to be identified at the injection points.
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service
