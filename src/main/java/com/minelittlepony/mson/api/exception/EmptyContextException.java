package com.minelittlepony.mson.api.exception;

public class EmptyContextException extends UnsupportedOperationException {
    private static final long serialVersionUID = 1971151950068660369L;

    public EmptyContextException(String message) {
        super("Context is empty - " + message);
    }
}
