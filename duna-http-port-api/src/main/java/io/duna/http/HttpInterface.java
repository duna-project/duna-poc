/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.http;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Repeatable(HttpInterfaces.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpInterface {

    HttpMethod method() default HttpMethod.GET;

    String path() default "";
}
