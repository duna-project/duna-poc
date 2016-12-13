package io.duna.web.annotations;

import java.lang.annotation.*;

/**
 * Created by carlos on 13/12/16.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {

    Method value() default Method.GET;

    enum Method {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE, CONNECT
    }
}
