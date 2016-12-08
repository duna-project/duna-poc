/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.inject

import io.duna.core.service.Contract
import io.duna.core.service.Service

import javax.inject.Inject

class DependsOnLocalService {
  @Inject
  LocalService localService
}

class DependsOnRemoteService {
  @Inject
  RemoteService remoteService
}

@Contract
interface LocalService {}

@Contract
interface RemoteService {}

class NotServiceImpl implements RemoteService {}

@Service
class LocalServiceImpl implements LocalService {}
