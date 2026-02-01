package com.noisevisionsoftware.vitema.exception;

public class InvitationAlreadyUsedException extends RuntimeException {
    public InvitationAlreadyUsedException(String message) {
        super(message);
    }
}
