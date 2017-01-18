/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.extend;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotates extensions defining ports to the services cluster.
 *
 * @author <a href="mailto:cemelo@redime.com.br">Carlos Eduardo Melo</a>
 */
@Qualifier
@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Port {
}
