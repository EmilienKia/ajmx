package com.github.emilienkia.ajmx.exceptions;

public class NotAnAMBean extends RuntimeException {

    public NotAnAMBean() {
    }

    public NotAnAMBean(String message) {
        super(message);
    }

    public NotAnAMBean(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAnAMBean(Throwable cause) {
        super(cause);
    }
}
