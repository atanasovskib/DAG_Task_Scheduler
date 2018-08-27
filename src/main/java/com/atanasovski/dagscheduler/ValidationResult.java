package com.atanasovski.dagscheduler;

import java.util.Objects;

public class ValidationResult {
    public final boolean isOk;
    public final String message;

    private ValidationResult(boolean isOk, String message) {
        this.isOk = isOk;
        this.message = message;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, Objects.requireNonNull(errorMessage));
    }

    public boolean isNotOk() {
        return !isOk;
    }

    public String message() {
        return message;
    }
}
