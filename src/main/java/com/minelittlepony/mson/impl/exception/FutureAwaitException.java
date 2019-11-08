package com.minelittlepony.mson.impl.exception;

public class FutureAwaitException extends RuntimeException {
    private static final long serialVersionUID = 8800010362946687756L;

    public FutureAwaitException(Exception cause) {
        super("Async data could not be loaded when requested", cause);
    }
}
