/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package com.google.inject;

import java.lang.reflect.Type;

/**
 * A type literal from an unchecked type.
 */
public class UnsafeTypeLiteral extends TypeLiteral<Object> {

    public UnsafeTypeLiteral(Type type) {
        super(type);
    }
}
