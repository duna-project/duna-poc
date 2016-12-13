package io.duna.core.service;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotates an injection point where the local service instances will be injected.
 */
@Qualifier
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalServices {
}
