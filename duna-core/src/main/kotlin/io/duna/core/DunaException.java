package io.duna.core;

/**
 * Created by carlos on 26/10/16.
 */
public class DunaException extends RuntimeException {
    public DunaException(Throwable t) {
        super(t);
    }

    public DunaException(String msg) {
        super(msg);
    }
}
