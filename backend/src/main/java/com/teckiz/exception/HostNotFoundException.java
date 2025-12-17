package com.teckiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class HostNotFoundException extends RuntimeException {

    public HostNotFoundException() {
        super("Website is currently unavailable due to non-payment.");
    }

    public HostNotFoundException(String message) {
        super(message);
    }
}

