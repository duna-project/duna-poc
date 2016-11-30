package io.duna.core.service.invocation;

/**
 * Created by carlos on 29/11/16.
 */
public interface CallableServiceProxy {
    <T> T invoke(Object target, Object ... parameters);
}
