package io.duna.core.service;

import java.lang.annotation.*;

/**
 * The service address inside the cluster.
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Address {
    String value();
}
