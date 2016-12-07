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