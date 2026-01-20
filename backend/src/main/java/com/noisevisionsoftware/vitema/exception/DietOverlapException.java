package com.noisevisionsoftware.vitema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DietOverlapException extends RuntimeException {
    public DietOverlapException(String message) {
        super(message);
    }
}
