package com.example.weighttogo.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for User model class.
 * Tests FR1.1 - User Registration data model.
 */
public class UserTest {

    @Test
    public void test_defaultConstructor_createsUserObject() {
        // ARRANGE & ACT
        User user = new User();

        // ASSERT
        assertNotNull("User object should not be null", user);
    }

    @Test
    public void test_getUserId_defaultValue_returnsZero() {
        // ARRANGE
        User user = new User();

        // ACT
        long userId = user.getUserId();

        // ASSERT
        assertEquals("Default userId should be 0", 0L, userId);
    }

    @Test
    public void test_setUserId_withValidId_setsValue() {
        // ARRANGE
        User user = new User();
        long expectedId = 42L;

        // ACT
        user.setUserId(expectedId);

        // ASSERT
        assertEquals("UserId should be set correctly", expectedId, user.getUserId());
    }

    @Test
    public void test_setUsername_withValidUsername_setsValue() {
        // ARRANGE
        User user = new User();
        String expectedUsername = "johndoe";

        // ACT
        user.setUsername(expectedUsername);

        // ASSERT
        assertEquals("Username should be set correctly", expectedUsername, user.getUsername());
    }

    @Test
    public void test_setPasswordHash_withValidHash_setsValue() {
        // ARRANGE
        User user = new User();
        String expectedHash = "abc123def456";

        // ACT
        user.setPasswordHash(expectedHash);

        // ASSERT
        assertEquals("PasswordHash should be set correctly", expectedHash, user.getPasswordHash());
    }

    @Test
    public void test_setSalt_withValidSalt_setsValue() {
        // ARRANGE
        User user = new User();
        String expectedSalt = "random16byteSalt";

        // ACT
        user.setSalt(expectedSalt);

        // ASSERT
        assertEquals("Salt should be set correctly", expectedSalt, user.getSalt());
    }

    @Test
    public void test_setCreatedAt_withValidDate_setsValue() {
        // ARRANGE
        User user = new User();
        String expectedDate = "2025-12-09 10:30:00";

        // ACT
        user.setCreatedAt(expectedDate);

        // ASSERT
        assertEquals("CreatedAt should be set correctly", expectedDate, user.getCreatedAt());
    }

    @Test
    public void test_setLastLogin_withValidDate_setsValue() {
        // ARRANGE
        User user = new User();
        String expectedDate = "2025-12-09 14:45:00";

        // ACT
        user.setLastLogin(expectedDate);

        // ASSERT
        assertEquals("LastLogin should be set correctly", expectedDate, user.getLastLogin());
    }

    @Test
    public void test_fullConstructor_withAllFields_createsUser() {
        // ARRANGE
        long expectedUserId = 1L;
        String expectedUsername = "testuser";
        String expectedPasswordHash = "hashedpassword123";
        String expectedSalt = "saltystring";
        String expectedCreatedAt = "2025-12-09 10:00:00";
        String expectedLastLogin = "2025-12-09 15:00:00";

        // ACT
        User user = new User(expectedUserId, expectedUsername, expectedPasswordHash,
                           expectedSalt, expectedCreatedAt, expectedLastLogin);

        // ASSERT
        assertEquals("UserId should match", expectedUserId, user.getUserId());
        assertEquals("Username should match", expectedUsername, user.getUsername());
        assertEquals("PasswordHash should match", expectedPasswordHash, user.getPasswordHash());
        assertEquals("Salt should match", expectedSalt, user.getSalt());
        assertEquals("CreatedAt should match", expectedCreatedAt, user.getCreatedAt());
        assertEquals("LastLogin should match", expectedLastLogin, user.getLastLogin());
    }

    @Test
    public void test_toString_returnsNonNullString() {
        // ARRANGE
        User user = new User();
        user.setUserId(1L);
        user.setUsername("johndoe");

        // ACT
        String result = user.toString();

        // ASSERT
        assertNotNull("toString should return non-null value", result);
        assertTrue("toString should contain username", result.contains("johndoe"));
        assertTrue("toString should contain userId", result.contains("1"));
    }

    @Test
    public void test_toString_doesNotExposePasswordHash() {
        // ARRANGE
        User user = new User();
        user.setPasswordHash("supersecret123");
        user.setSalt("randomsalt456");

        // ACT
        String result = user.toString();

        // ASSERT
        assertFalse("toString should NOT expose password hash", result.contains("supersecret123"));
        assertFalse("toString should NOT expose salt", result.contains("randomsalt456"));
    }
}
