/*
 * Copyright (c) 2016 Duna Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpPath {
    String value() default "/";
}