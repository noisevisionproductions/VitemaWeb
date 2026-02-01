package com.noisevisionsoftware.vitema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyHasTrainerException extends RuntimeException {

    public UserAlreadyHasTrainerException(String message) {
        super(message);
    }
}