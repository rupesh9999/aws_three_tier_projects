package com.streamflix.common.exception;

/**
 * Exception thrown when user does not have permission to perform an action
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("You do not have permission to perform this action");
    }
}
