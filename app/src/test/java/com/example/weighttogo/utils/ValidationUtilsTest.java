package com.example.weighttogo.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for ValidationUtils class.
 * Tests input validation for usernames and passwords.
 */
public class ValidationUtilsTest {

    // =============================================================================================
    // USERNAME VALIDATION TESTS (6 tests)
    // =============================================================================================

    /**
     * Tests that isValidUsername() returns true for valid username.
     * Valid: 3-20 characters, alphanumeric and underscore only.
     */
    @Test
    public void test_isValidUsername_withValidInput_returnsTrue() {
        // ARRANGE
        String validUsername = "testuser_123";

        // ACT
        boolean result = ValidationUtils.isValidUsername(validUsername);

        // ASSERT
        assertTrue("Valid username should return true", result);
    }

    /**
     * Tests that isValidUsername() returns false for short username.
     * Username must be at least 3 characters.
     */
    @Test
    public void test_isValidUsername_withShortInput_returnsFalse() {
        // ARRANGE
        String shortUsername = "ab";  // Only 2 characters

        // ACT
        boolean result = ValidationUtils.isValidUsername(shortUsername);

        // ASSERT
        assertFalse("Username with less than 3 characters should be invalid", result);
    }

    /**
     * Tests that isValidUsername() returns false for long username.
     * Username must be at most 20 characters.
     */
    @Test
    public void test_isValidUsername_withLongInput_returnsFalse() {
        // ARRANGE
        String longUsername = "thisusernameiswaytoolong123";  // More than 20 characters

        // ACT
        boolean result = ValidationUtils.isValidUsername(longUsername);

        // ASSERT
        assertFalse("Username with more than 20 characters should be invalid", result);
    }

    /**
     * Tests that isValidUsername() returns false for username with special characters.
     * Only alphanumeric and underscore allowed.
     */
    @Test
    public void test_isValidUsername_withSpecialChars_returnsFalse() {
        // ARRANGE
        String specialCharsUsername = "user@name!";  // Contains @ and !

        // ACT
        boolean result = ValidationUtils.isValidUsername(specialCharsUsername);

        // ASSERT
        assertFalse("Username with special characters should be invalid", result);
    }

    /**
     * Tests that isValidUsername() returns false for null username.
     */
    @Test
    public void test_isValidUsername_withNull_returnsFalse() {
        // ARRANGE
        String nullUsername = null;

        // ACT
        boolean result = ValidationUtils.isValidUsername(nullUsername);

        // ASSERT
        assertFalse("Null username should be invalid", result);
    }

    /**
     * Tests that isValidUsername() returns false for empty username.
     */
    @Test
    public void test_isValidUsername_withEmpty_returnsFalse() {
        // ARRANGE
        String emptyUsername = "";

        // ACT
        boolean result = ValidationUtils.isValidUsername(emptyUsername);

        // ASSERT
        assertFalse("Empty username should be invalid", result);
    }

    // =============================================================================================
    // PASSWORD VALIDATION TESTS (6 tests)
    // =============================================================================================

    /**
     * Tests that isValidPassword() returns true for valid password.
     * Valid: 6+ characters with at least one digit.
     */
    @Test
    public void test_isValidPassword_withValidInput_returnsTrue() {
        // ARRANGE
        String validPassword = "Test123";  // 7 characters with digits

        // ACT
        boolean result = ValidationUtils.isValidPassword(validPassword);

        // ASSERT
        assertTrue("Valid password should return true", result);
    }

    /**
     * Tests that isValidPassword() returns false for short password.
     * Password must be at least 6 characters.
     */
    @Test
    public void test_isValidPassword_withShortInput_returnsFalse() {
        // ARRANGE
        String shortPassword = "Ab1";  // Only 3 characters

        // ACT
        boolean result = ValidationUtils.isValidPassword(shortPassword);

        // ASSERT
        assertFalse("Password with less than 6 characters should be invalid", result);
    }

    /**
     * Tests that isValidPassword() returns false for password without number.
     * Password must contain at least one digit.
     */
    @Test
    public void test_isValidPassword_withNoNumber_returnsFalse() {
        // ARRANGE
        String noNumberPassword = "TestPassword";  // No digits

        // ACT
        boolean result = ValidationUtils.isValidPassword(noNumberPassword);

        // ASSERT
        assertFalse("Password without digits should be invalid", result);
    }

    /**
     * Tests that isValidPassword() returns false for null password.
     */
    @Test
    public void test_isValidPassword_withNull_returnsFalse() {
        // ARRANGE
        String nullPassword = null;

        // ACT
        boolean result = ValidationUtils.isValidPassword(nullPassword);

        // ASSERT
        assertFalse("Null password should be invalid", result);
    }

    /**
     * Tests that isValidPassword() returns false for empty password.
     */
    @Test
    public void test_isValidPassword_withEmpty_returnsFalse() {
        // ARRANGE
        String emptyPassword = "";

        // ACT
        boolean result = ValidationUtils.isValidPassword(emptyPassword);

        // ASSERT
        assertFalse("Empty password should be invalid", result);
    }

    /**
     * Tests that isValidPassword() returns true for password with only numbers.
     * Edge case: Passwords with only numbers are valid (6+ digits).
     */
    @Test
    public void test_isValidPassword_withOnlyNumbers_returnsTrue() {
        // ARRANGE
        String onlyNumbersPassword = "123456";  // Valid: 6+ characters, all digits

        // ACT
        boolean result = ValidationUtils.isValidPassword(onlyNumbersPassword);

        // ASSERT
        assertTrue("Password with only numbers (6+ chars) should be valid", result);
    }

    // =============================================================================================
    // NULL/EMPTY CHECKING TESTS (1 test with multiple cases)
    // =============================================================================================

    /**
     * Tests that isNullOrEmpty() correctly identifies null, empty, and whitespace-only strings.
     * This utility method is used throughout the codebase for consistent null checking.
     */
    @Test
    public void test_isNullOrEmpty_withVariousInputs_returnsCorrectValue() {
        // ARRANGE & ACT & ASSERT

        // Null string should return true
        assertTrue("null should return true",
                ValidationUtils.isNullOrEmpty(null));

        // Empty string should return true
        assertTrue("Empty string should return true",
                ValidationUtils.isNullOrEmpty(""));

        // Whitespace only should return true
        assertTrue("Whitespace-only string should return true",
                ValidationUtils.isNullOrEmpty("   "));

        // Valid string should return false
        assertFalse("Non-empty string should return false",
                ValidationUtils.isNullOrEmpty("test"));

        // String with whitespace padding should return false (trimmed first)
        assertFalse("String with whitespace padding should return false",
                ValidationUtils.isNullOrEmpty(" test "));
    }
}
