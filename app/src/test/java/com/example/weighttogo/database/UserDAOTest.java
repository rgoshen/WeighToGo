package com.example.weighttogo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.weighttogo.models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for UserDAO following strict TDD.
 * Tests CRUD operations for User model.
 *
 * TDD Approach: Write ONE failing test at a time, implement minimal code to pass,
 * then move to next test.
 */
@RunWith(RobolectricTestRunner.class)
public class UserDAOTest {

    private Context context;
    private WeighToGoDBHelper dbHelper;
    private UserDAO userDAO;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws DatabaseException {
        context = RuntimeEnvironment.getApplication();
        dbHelper = WeighToGoDBHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
        userDAO = new UserDAO(dbHelper);
    }

    @After
    public void tearDown() {
        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        } finally {
            dbHelper = null;
            userDAO = null;
            context.deleteDatabase("weigh_to_go.db");
            WeighToGoDBHelper.resetInstance();
        }
    }

    /**
     * Test 1: insertUser() should insert a valid user and return user ID > 0
     */
    @Test
    public void test_insertUser_withValidData_returnsUserId() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashed_password_123");
        user.setSalt("random_salt_456");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        // ACT
        long userId = userDAO.insertUser(user);

        // ASSERT
        assertTrue("User ID should be greater than 0", userId > 0);
        user.setUserId(userId);

        // Verify user was actually inserted by querying database
        User retrievedUser = userDAO.getUserById(userId);
        assertNotNull("Retrieved user should not be null", retrievedUser);
        assertEquals("Username should match", "testuser", retrievedUser.getUsername());
    }

    /**
     * Test 2: getUserByUsername() should retrieve user by username
     */
    @Test
    public void test_getUserByUsername_withExistingUser_returnsUser() throws DatabaseException {
        // ARRANGE - Insert a user first
        User user = new User();
        user.setUsername("johndoe");
        user.setPasswordHash("hashed_pass");
        user.setSalt("salt123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        long userId = userDAO.insertUser(user);
        user.setUserId(userId);

        // ACT
        User retrievedUser = userDAO.getUserByUsername("johndoe");

        // ASSERT
        assertNotNull("User should be found", retrievedUser);
        assertEquals("User ID should match", userId, retrievedUser.getUserId());
        assertEquals("Username should match", "johndoe", retrievedUser.getUsername());
    }

    /**
     * Test 3: getUserByUsername() returns null for non-existent username
     */
    @Test
    public void test_getUserByUsername_withNonExistentUser_returnsNull() throws DatabaseException {
        // ACT
        User user = userDAO.getUserByUsername("nonexistent");

        // ASSERT
        assertNull("Should return null for non-existent user", user);
    }

    /**
     * Test 4: usernameExists() returns true for existing username
     */
    @Test
    public void test_usernameExists_withExistingUsername_returnsTrue() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("existinguser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        userDAO.insertUser(user);

        // ACT
        boolean exists = userDAO.usernameExists("existinguser");

        // ASSERT
        assertTrue("Username should exist", exists);
    }

    /**
     * Test 5: usernameExists() returns false for non-existent username
     */
    @Test
    public void test_usernameExists_withNonExistentUsername_returnsFalse() throws DatabaseException {
        // ACT
        boolean exists = userDAO.usernameExists("nonexistent");

        // ASSERT
        assertFalse("Username should not exist", exists);
    }

    /**
     * Test 6: updateLastLogin() updates the last_login timestamp
     */
    @Test
    public void test_updateLastLogin_updatesTimestamp() throws DatabaseException {
        // ARRANGE - Insert a user
        User user = new User();
        user.setUsername("loginuser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        long userId = userDAO.insertUser(user);

        LocalDateTime loginTime = LocalDateTime.now();

        // ACT
        int rowsAffected = userDAO.updateLastLogin(userId, loginTime);

        // ASSERT
        assertEquals("Should update 1 row", 1, rowsAffected);

        // Verify the update
        User updatedUser = userDAO.getUserById(userId);
        assertNotNull("Updated user should exist", updatedUser);
        assertNotNull("Last login should be set", updatedUser.getLastLogin());
    }

    /**
     * Test 7: deleteUser() removes user from database
     */
    @Test
    public void test_deleteUser_removesUser() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("deletetest");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        long userId = userDAO.insertUser(user);

        // ACT
        int rowsDeleted = userDAO.deleteUser(userId);

        // ASSERT
        assertEquals("Should delete 1 row", 1, rowsDeleted);

        // Verify user no longer exists
        User deletedUser = userDAO.getUserById(userId);
        assertNull("User should be deleted", deletedUser);
    }

    // ========== EDGE CASE TESTS ==========

    /**
     * Test 8: insertUser() with duplicate username violates UNIQUE constraint
     */
    @Test
    public void test_insertUser_withDuplicateUsername_violatesUniqueConstraint() throws DatabaseException {
        // ARRANGE
        User user1 = new User();
        user1.setUsername("duplicateuser");
        user1.setPasswordHash("hash1");
        user1.setSalt("salt1");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setActive(true);

        User user2 = new User();
        user2.setUsername("duplicateuser"); // Same username
        user2.setPasswordHash("hash2");
        user2.setSalt("salt2");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setActive(true);

        // ACT
        long userId1 = userDAO.insertUser(user1);
        assertTrue("First user should be inserted successfully", userId1 > 0);

        // ACT & ASSERT - second insert should throw DuplicateUsernameException
        boolean exceptionThrown = false;
        try {
            userDAO.insertUser(user2); // Should throw DuplicateUsernameException
            fail("Expected DuplicateUsernameException to be thrown");
        } catch (DuplicateUsernameException e) {
            exceptionThrown = true;
            assertTrue("Exception message should mention the username",
                    e.getMessage().contains("duplicateuser"));
        }

        assertTrue("DuplicateUsernameException should have been thrown", exceptionThrown);
    }

    /**
     * Test 9: getUserById() with non-existent ID returns null
     */
    @Test
    public void test_getUserById_withNonExistentId_returnsNull() throws DatabaseException {
        // ACT
        User user = userDAO.getUserById(99999);

        // ASSERT
        assertNull("Should return null for non-existent user ID", user);
    }

    /**
     * Test 10: updateLastLogin() with non-existent user ID returns 0
     */
    @Test
    public void test_updateLastLogin_withNonExistentUserId_returnsZero() throws DatabaseException {
        // ACT
        int rowsAffected = userDAO.updateLastLogin(99999, LocalDateTime.now());

        // ASSERT
        assertEquals("Updating non-existent user should return 0 rows affected", 0, rowsAffected);
    }

    /**
     * Test 11: deleteUser() with non-existent ID returns 0
     */
    @Test
    public void test_deleteUser_withNonExistentUserId_returnsZero() throws DatabaseException {
        // ACT
        int rowsDeleted = userDAO.deleteUser(99999);

        // ASSERT
        assertEquals("Deleting non-existent user should return 0 rows deleted", 0, rowsDeleted);
    }

    /**
     * Test 12: insertUser() with special characters in username
     */
    @Test
    public void test_insertUser_withSpecialCharactersInUsername_insertsSuccessfully() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("user_with-special.chars@123");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        // ACT
        long userId = userDAO.insertUser(user);
        User retrieved = userDAO.getUserById(userId);

        // ASSERT
        assertTrue("User should be inserted", userId > 0);
        assertNotNull("User should be retrieved", retrieved);
        assertEquals("Special characters in username should be preserved", "user_with-special.chars@123", retrieved.getUsername());
    }

    /**
     * Test 13: insertUser() with very long username
     */
    @Test
    public void test_insertUser_withVeryLongUsername_insertsSuccessfully() throws DatabaseException {
        // ARRANGE
        StringBuilder longUsername = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longUsername.append("a");
        }
        User user = new User();
        user.setUsername(longUsername.toString());
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        // ACT
        long userId = userDAO.insertUser(user);
        User retrieved = userDAO.getUserById(userId);

        // ASSERT
        assertTrue("User should be inserted", userId > 0);
        assertNotNull("User should be retrieved", retrieved);
        assertEquals("Long username should be preserved", longUsername.toString(), retrieved.getUsername());
    }

    /**
     * Test 14: insertUser() with all optional fields populated
     */
    @Test
    public void test_insertUser_withAllOptionalFields_insertsSuccessfully() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("completeuser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setEmail("user@example.com");
        user.setPhoneNumber("+1234567890");
        user.setDisplayName("Complete User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setActive(true);

        // ACT
        long userId = userDAO.insertUser(user);
        User retrieved = userDAO.getUserById(userId);

        // ASSERT
        assertTrue("User should be inserted", userId > 0);
        assertNotNull("User should be retrieved", retrieved);
        assertEquals("Email should be preserved", "user@example.com", retrieved.getEmail());
        assertEquals("Phone number should be preserved", "+1234567890", retrieved.getPhoneNumber());
        assertEquals("Display name should be preserved", "Complete User", retrieved.getDisplayName());
        assertNotNull("Last login should be preserved", retrieved.getLastLogin());
    }

    /**
     * Test 15: insertUser() with special characters in optional fields
     */
    @Test
    public void test_insertUser_withSpecialCharactersInOptionalFields_insertsSuccessfully() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("specialuser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setEmail("user+tag@example.com");
        user.setDisplayName("User 🎉 Name");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        // ACT
        long userId = userDAO.insertUser(user);
        User retrieved = userDAO.getUserById(userId);

        // ASSERT
        assertTrue("User should be inserted", userId > 0);
        assertNotNull("User should be retrieved", retrieved);
        assertEquals("Email with + should be preserved", "user+tag@example.com", retrieved.getEmail());
        assertEquals("Display name with emoji should be preserved", "User 🎉 Name", retrieved.getDisplayName());
    }

    /**
     * Test 16: deleteUser() cascades to weight entries and goals
     */
    @Test
    public void test_deleteUser_cascadesToRelatedRecords() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("cascadetest");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        long userId = userDAO.insertUser(user);

        // Insert related weight entry (this will be tested in integration)
        // For now, just verify user deletion works

        // ACT
        int rowsDeleted = userDAO.deleteUser(userId);

        // ASSERT
        assertEquals("Should delete 1 row", 1, rowsDeleted);
        User deletedUser = userDAO.getUserById(userId);
        assertNull("User should no longer exist", deletedUser);
        // Note: CASCADE DELETE for weight_entries and goal_weights verified in integration tests
    }

    /**
     * Test 17: usernameExists() is case-sensitive
     */
    @Test
    public void test_usernameExists_isCaseSensitive() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("TestUser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        userDAO.insertUser(user);

        // ACT
        boolean existsExactCase = userDAO.usernameExists("TestUser");
        boolean existsLowerCase = userDAO.usernameExists("testuser");
        boolean existsUpperCase = userDAO.usernameExists("TESTUSER");

        // ASSERT
        assertTrue("Exact case should exist", existsExactCase);
        assertFalse("Lowercase variant should not exist (case-sensitive)", existsLowerCase);
        assertFalse("Uppercase variant should not exist (case-sensitive)", existsUpperCase);
    }

    /**
     * Test 18: getUserByUsername() is case-sensitive
     */
    @Test
    public void test_getUserByUsername_isCaseSensitive() throws DatabaseException {
        // ARRANGE
        User user = new User();
        user.setUsername("CaseSensitive");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        userDAO.insertUser(user);

        // ACT
        User exactMatch = userDAO.getUserByUsername("CaseSensitive");
        User lowerMatch = userDAO.getUserByUsername("casesensitive");

        // ASSERT
        assertNotNull("Exact case should return user", exactMatch);
        assertNull("Different case should not return user (case-sensitive)", lowerMatch);
    }
}
