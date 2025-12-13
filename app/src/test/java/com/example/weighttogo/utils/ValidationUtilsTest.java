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

    // =============================================================================================
    // PHONE NUMBER VALIDATION TESTS (11 tests) - Phase 7.1
    // =============================================================================================

    /**
     * Tests that isValidPhoneNumber() returns true for valid US phone number.
     * Valid: 10 digits without country code.
     */
    @Test
    public void test_isValidPhoneNumber_withValidUSNumber_returnsTrue() {
        // ARRANGE
        String validPhone = "2025551234";  // 10 digits (US format without country code)

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(validPhone);

        // ASSERT
        assertTrue("Valid 10-digit US phone should return true", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns true for valid E.164 format.
     * Valid: Phone number with + prefix and country code.
     */
    @Test
    public void test_isValidPhoneNumber_withValidE164Format_returnsTrue() {
        // ARRANGE
        String validE164 = "+12025551234";  // E.164 format with country code

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(validE164);

        // ASSERT
        assertTrue("Valid E.164 phone number should return true", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for null input.
     */
    @Test
    public void test_isValidPhoneNumber_withNullInput_returnsFalse() {
        // ARRANGE
        String nullPhone = null;

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(nullPhone);

        // ASSERT
        assertFalse("Null phone number should return false", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for empty string.
     */
    @Test
    public void test_isValidPhoneNumber_withEmptyString_returnsFalse() {
        // ARRANGE
        String emptyPhone = "";

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(emptyPhone);

        // ASSERT
        assertFalse("Empty phone number should return false", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for phone number too short.
     * Phone must be at least 10 digits.
     */
    @Test
    public void test_isValidPhoneNumber_withTooShort_returnsFalse() {
        // ARRANGE
        String tooShort = "123456789";  // Only 9 digits

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(tooShort);

        // ASSERT
        assertFalse("Phone number with less than 10 digits should return false", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for phone number too long.
     * Phone must be at most 15 digits.
     */
    @Test
    public void test_isValidPhoneNumber_withTooLong_returnsFalse() {
        // ARRANGE
        String tooLong = "12345678901234567";  // 17 digits (way too many)

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(tooLong);

        // ASSERT
        assertFalse("Phone number with more than 15 digits should return false", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for phone with letters.
     * Only digits and + prefix are allowed.
     */
    @Test
    public void test_isValidPhoneNumber_withLetters_returnsFalse() {
        // ARRANGE
        String withLetters = "202-555-ABCD";  // Contains letters

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(withLetters);

        // ASSERT
        assertFalse("Phone number with letters should return false", result);
    }

    /**
     * Tests that isValidPhoneNumber() returns false for phone with special characters.
     * Dashes and spaces are NOT allowed in E.164 format.
     */
    @Test
    public void test_isValidPhoneNumber_withSpecialChars_returnsFalse() {
        // ARRANGE
        String withSpecialChars = "202-555-1234";  // Dashes are NOT allowed in E.164

        // ACT
        boolean result = ValidationUtils.isValidPhoneNumber(withSpecialChars);

        // ASSERT
        assertFalse("Phone number with dashes/special chars should return false", result);
    }

    /**
     * Tests that formatPhoneE164() returns E.164 format for valid US number.
     * 10-digit US number should be formatted with +1 prefix.
     */
    @Test
    public void test_formatPhoneE164_withValidUSNumber_returnsE164() {
        // ARRANGE
        String usNumber = "2025551234";  // 10 digits
        String expected = "+12025551234";  // Expected E.164 format

        // ACT
        String result = ValidationUtils.formatPhoneE164(usNumber);

        // ASSERT
        assertTrue("US number should be formatted as E.164", expected.equals(result));
    }

    /**
     * Tests that formatPhoneE164() returns unchanged for already E.164 format.
     * Phone already in E.164 should not be modified.
     */
    @Test
    public void test_formatPhoneE164_withAlreadyE164_returnsUnchanged() {
        // ARRANGE
        String e164Number = "+12025551234";  // Already in E.164 format

        // ACT
        String result = ValidationUtils.formatPhoneE164(e164Number);

        // ASSERT
        assertTrue("E.164 number should remain unchanged", e164Number.equals(result));
    }

    /**
     * Tests that formatPhoneE164() returns null for invalid number.
     * Invalid phone numbers cannot be formatted.
     */
    @Test
    public void test_formatPhoneE164_withInvalidNumber_returnsNull() {
        // ARRANGE
        String invalidNumber = "abc123";  // Invalid format

        // ACT
        String result = ValidationUtils.formatPhoneE164(invalidNumber);

        // ASSERT
        assertTrue("Invalid number should return null", result == null);
    }
}
