/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package com.google.inject

import java.lang.reflect.Type

internal class UnsafeTypeLiteral(type: Type) : TypeLiteral<Any>(type)
