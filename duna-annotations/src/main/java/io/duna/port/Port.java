package io.duna.port;

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
