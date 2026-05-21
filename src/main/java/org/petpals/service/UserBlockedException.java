package org.petpals.service;

public class UserBlockedException extends RuntimeException {
    private Long userId;

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException(String message, Long userId) {
        super(message);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
