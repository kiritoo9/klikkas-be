package com.klikkas.exception;

public class NotFoundException 
    extends RuntimeException {

    public final String error;

    public NotFoundException(
        String message,
        String error
    ) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
