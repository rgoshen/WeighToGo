package com.example.weighttogo.activities;

import static org.junit.Assert.*;

import android.content.Context;

import com.example.weighttogo.database.DatabaseException;
import com.example.weighttogo.database.DuplicateUsernameException;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.models.User;
import com.example.weighttogo.utils.PasswordUtils;
import com.example.weighttogo.utils.SessionManager;
import com.example.weighttogo.utils.ValidationUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDateTime;

/**
 * Integration tests for LoginActivity authentication flows.
 *
 * These tests verify end-to-end integration between multiple components:
 * - ValidationUtils (input validation)
 * - PasswordUtils (password hashing/verification)
 * - UserDAO (database operations)
 * - SessionManager (session persistence)
 * - LoginActivity (navigation)
 *
 * **Test Strategy:**
 * - Hybrid approach: minimal integration tests now (2 tests)
 * - Comprehensive scenario testing deferred to Phase 8
 * - Focus on critical flows: registration and login
 *
 * **Coverage:**
 * - ✅ Registration flow: validation → hash → DAO insert → session → navigation
 * - ✅ Login flow: validation → DAO query → password verify → session → navigation
 * - ⏸ Deferred to Phase 8: edge cases, errors, session persistence, logout
 */
@RunWith(RobolectricTestRunner.class)
public class LoginActivityIntegrationTest {

    private Context context;
    private WeighToGoDBHelper dbHelper;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        dbHelper = WeighToGoDBHelper.getInstance(context);
        userDAO = new UserDAO(dbHelper);
        sessionManager = SessionManager.getInstance(context);

        // Clear any existing session
        sessionManager.logout();
    }

    @After
    public void tearDown() {
        // Clean up session
        sessionManager.logout();

        // Close database
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Test end-to-end registration flow.
     *
     * Verifies integration across all layers:
     * 1. Input validation (ValidationUtils)
     * 2. Salt generation (PasswordUtils)
     * 3. Password hashing (PasswordUtils)
     * 4. Database insertion (UserDAO)
     * 5. Session creation (SessionManager)
     * 6. Navigation would occur (verified by session state)
     *
     * This simulates the complete handleRegister() flow in LoginActivity.
     */
    @Test
    public void test_registrationFlow_createsUserAndNavigates() throws DuplicateUsernameException, DatabaseException {
        // ARRANGE - Simulate user input
        String username = "testuser123";
        String password = "Test123";

        // ACT 1: Validate input (ValidationUtils)
        boolean usernameValid = ValidationUtils.isValidUsername(username);
        boolean passwordValid = ValidationUtils.isValidPassword(password);

        assertTrue("Username should be valid", usernameValid);
        assertTrue("Password should be valid", passwordValid);

        // ACT 2: Check username doesn't exist (UserDAO)
        boolean usernameExists = userDAO.usernameExists(username);
        assertFalse("Username should not exist yet", usernameExists);

        // ACT 3: Generate salt and hash password (PasswordUtils)
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        assertNotNull("Salt should be generated", salt);
        assertNotNull("Password should be hashed", passwordHash);

        // ACT 4: Create User object and insert into database (UserDAO)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordHash);
        newUser.setSalt(salt);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setActive(true);

        long userId = userDAO.insertUser(newUser);
        assertTrue("User ID should be greater than 0", userId > 0);

        // ACT 5: Update user object with ID and create session (SessionManager)
        newUser.setUserId(userId);
        sessionManager.createSession(newUser);

        // ASSERT: Verify session was created (indicates navigation would succeed)
        assertTrue("User should be logged in", sessionManager.isLoggedIn());
        assertEquals("Session should have correct user ID", userId, sessionManager.getCurrentUserId());

        User sessionUser = sessionManager.getCurrentUser();
        assertNotNull("Session should have current user", sessionUser);
        assertEquals("Session username should match", username, sessionUser.getUsername());

        // ASSERT: Verify user can be retrieved from database
        User retrievedUser = userDAO.getUserByUsername(username);
        assertNotNull("User should exist in database", retrievedUser);
        assertEquals("Retrieved username should match", username, retrievedUser.getUsername());
        assertEquals("Retrieved userId should match", userId, retrievedUser.getUserId());
    }

    /**
     * Test end-to-end login flow.
     *
     * Verifies integration across all layers:
     * 1. Input validation (ValidationUtils)
     * 2. User lookup (UserDAO)
     * 3. Password verification (PasswordUtils)
     * 4. Last login update (UserDAO)
     * 5. Session creation (SessionManager)
     * 6. Navigation would occur (verified by session state)
     *
     * This simulates the complete handleSignIn() flow in LoginActivity.
     */
    @Test
    public void test_loginFlow_authenticatesAndNavigates() throws DuplicateUsernameException, DatabaseException {
        // ARRANGE - Create test user in database
        String username = "loginuser456";
        String password = "Pass456";

        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        User existingUser = new User();
        existingUser.setUsername(username);
        existingUser.setPasswordHash(passwordHash);
        existingUser.setSalt(salt);
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setUpdatedAt(LocalDateTime.now());
        existingUser.setActive(true);

        long userId = userDAO.insertUser(existingUser);
        existingUser.setUserId(userId);

        // ACT 1: Validate input (ValidationUtils)
        boolean usernameValid = ValidationUtils.isValidUsername(username);
        boolean passwordValid = ValidationUtils.isValidPassword(password);

        assertTrue("Username should be valid", usernameValid);
        assertTrue("Password should be valid", passwordValid);

        // ACT 2: Query user from database (UserDAO)
        User user = userDAO.getUserByUsername(username);
        assertNotNull("User should exist in database", user);

        // ACT 3: Verify password (PasswordUtils)
        boolean passwordMatches = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        assertTrue("Password should match", passwordMatches);

        // ACT 4: Update last_login timestamp (UserDAO)
        LocalDateTime loginTime = LocalDateTime.now();
        int rowsUpdated = userDAO.updateLastLogin(user.getUserId(), loginTime);
        assertEquals("One row should be updated", 1, rowsUpdated);

        // ACT 5: Create session (SessionManager)
        sessionManager.createSession(user);

        // ASSERT: Verify session was created (indicates navigation would succeed)
        assertTrue("User should be logged in", sessionManager.isLoggedIn());
        assertEquals("Session should have correct user ID", userId, sessionManager.getCurrentUserId());

        User sessionUser = sessionManager.getCurrentUser();
        assertNotNull("Session should have current user", sessionUser);
        assertEquals("Session username should match", username, sessionUser.getUsername());

        // ASSERT: Verify last_login was updated
        User updatedUser = userDAO.getUserById(userId);
        assertNotNull("Updated user should exist", updatedUser);
        assertNotNull("Last login should be set", updatedUser.getLastLogin());
    }
}
