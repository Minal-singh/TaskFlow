package com.minal.taskflow.exceptions;

public class TaskNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Task not found";

    public TaskNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
