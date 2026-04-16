package com.studentbag.backend.notifications.exception;

public class InvalidNotificationRequestException extends RuntimeException {
    public InvalidNotificationRequestException(String message) {
        super(message);
    }
}