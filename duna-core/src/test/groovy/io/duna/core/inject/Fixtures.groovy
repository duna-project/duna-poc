/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import io.duna.core.service.Contract
import io.duna.core.service.Service

import javax.inject.Inject
import javax.inject.Qualifier
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Contract
interface LocalService {}

@Contract
interface RemoteService {}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@interface ServiceQualifier {}

@Service
class LocalServiceImpl implements LocalService {
  RemoteService remoteService

  @Inject
  LocalServiceImpl(@ServiceQualifier RemoteService remoteService) {
    this.remoteService = remoteService
  }
}
