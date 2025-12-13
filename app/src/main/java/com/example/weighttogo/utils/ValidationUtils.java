package com.example.weighttogo.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.regex.Pattern;

/**
 * Utility class for input validation (usernames, passwords, etc.).
 *
 * Provides validation methods for user inputs to ensure data integrity and security.
 * All validation methods are null-safe and return false for invalid inputs.
 *
 * **Validation Rules:**
 * - **Username**: 3-20 characters, alphanumeric and underscore only
 * - **Password**: Minimum 6 characters with at least one digit
 *
 * **Usage Example:**
 * <pre>
 * // Validate username
 * if (ValidationUtils.isValidUsername(username)) {
 *     // Proceed with registration
 * } else {
 *     // Show error message
 * }
 *
 * // Validate password
 * if (ValidationUtils.isValidPassword(password)) {
 *     // Proceed with authentication
 * } else {
 *     // Show error message
 * }
 * </pre>
 *
 * **Thread Safety:** All methods are static and thread-safe.
 */
public final class ValidationUtils {

    private static final String TAG = "ValidationUtils";

    /**
     * Username validation pattern: 3-20 characters, alphanumeric and underscore only.
     * Pattern: ^[a-zA-Z0-9_]{3,20}$
     * - ^ = start of string
     * - [a-zA-Z0-9_] = alphanumeric and underscore character class
     * - {3,20} = 3 to 20 characters
     * - $ = end of string
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /**
     * Minimum password length requirement.
     */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws AssertionError if instantiation is attempted via reflection
     */
    private ValidationUtils() {
        throw new AssertionError("ValidationUtils is a utility class and should not be instantiated");
    }

    // =============================================================================================
    // NULL/EMPTY CHECKING
    // =============================================================================================

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * This is a centralized null-checking method to eliminate duplicate code patterns
     * throughout the codebase (DRY principle).
     *
     * **Usage Example:**
     * <pre>
     * if (ValidationUtils.isNullOrEmpty(username)) {
     *     // Show error: "Username is required"
     *     return;
     * }
     * </pre>
     *
     * @param value the string to check
     * @return true if the string is null, empty (""), or whitespace-only ("   ")
     */
    public static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    // =============================================================================================
    // USERNAME & PASSWORD VALIDATION
    // =============================================================================================

    /**
     * Validates a username against security requirements.
     *
     * **Validation Rules:**
     * - Length: 3-20 characters
     * - Allowed characters: a-z, A-Z, 0-9, underscore (_)
     * - No spaces, special characters, or Unicode allowed
     *
     * **Security Note:** Username validation prevents SQL injection attempts
     * and ensures usernames are database-compatible.
     *
     * @param username the username to validate
     * @return true if valid, false if null, empty, or doesn't match pattern
     */
    public static boolean isValidUsername(@Nullable String username) {
        // Null or empty check
        if (isNullOrEmpty(username)) {
            Log.w(TAG, "isValidUsername: username is null or empty");
            return false;
        }

        // Pattern matching
        boolean isValid = USERNAME_PATTERN.matcher(username).matches();

        if (!isValid) {
            Log.w(TAG, "isValidUsername: username does not match pattern (3-20 chars, alphanumeric + underscore)");
        } else {
            Log.d(TAG, "isValidUsername: username is valid");
        }

        return isValid;
    }

    /**
     * Validates a password against security requirements.
     *
     * **Validation Rules:**
     * - Minimum length: 6 characters
     * - Must contain at least one digit (0-9)
     * - No maximum length (longer is better for security)
     *
     * **Security Note:** These are minimum requirements for Phase 2.
     * Future phases may add additional requirements:
     * - Uppercase letter
     * - Lowercase letter
     * - Special character
     * - Maximum length limit
     *
     * @param password the password to validate
     * @return true if valid, false if null, empty, too short, or missing digits
     */
    public static boolean isValidPassword(@Nullable String password) {
        // Null or empty check
        if (isNullOrEmpty(password)) {
            Log.w(TAG, "isValidPassword: password is null or empty");
            return false;
        }

        // Length check
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Log.w(TAG, "isValidPassword: password is too short (min " + MIN_PASSWORD_LENGTH + " chars)");
            return false;
        }

        // Must contain at least one digit
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasDigit) {
            Log.w(TAG, "isValidPassword: password does not contain a digit");
            return false;
        }

        Log.d(TAG, "isValidPassword: password is valid");
        return true;
    }
}
