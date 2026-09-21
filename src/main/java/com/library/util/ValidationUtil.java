package com.library.util;

import java.util.regex.Pattern;

/**
 * Stateless helper methods for validating common user input formats.
 * Kept separate from the service layer so validation rules can be
 * unit-tested and reused independently of business logic.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private ValidationUtil() {
        // static utility class; prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
