package com.github.emilienkia.ajmx.exceptions;

public class AJMXException extends RuntimeException {

    public AJMXException() {
    }

    public AJMXException(String message) {
        super(message);
    }

    public AJMXException(String message, Throwable cause) {
        super(message, cause);
    }

    public AJMXException(Throwable cause) {
        super(cause);
    }
}
