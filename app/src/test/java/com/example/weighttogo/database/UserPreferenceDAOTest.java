package com.example.weighttogo.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.example.weighttogo.models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDateTime;

/**
 * Test class for UserPreferenceDAO using Robolectric for Android SQLite testing.
 *
 * Follows strict Test-Driven Development (TDD) methodology:
 * - Tests written BEFORE implementation
 * - Red-Green-Refactor cycle
 * - 100% test coverage
 *
 * Tests the generic key-value preference storage with UPSERT pattern.
 */
@RunWith(RobolectricTestRunner.class)
public class UserPreferenceDAOTest {

    private WeighToGoDBHelper dbHelper;
    private UserPreferenceDAO userPreferenceDAO;
    private UserDAO userDAO;
    private long testUserId;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        dbHelper = WeighToGoDBHelper.getInstance(context);
        userPreferenceDAO = new UserPreferenceDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);

        // Create test user for foreign key relationships
        User testUser = createTestUser("testuser");
        try {
            testUserId = userDAO.insertUser(testUser);
            assertTrue("Test user should be created", testUserId > 0);
        } catch (DatabaseException e) {
            throw new RuntimeException("Failed to create test user", e);
        }
    }

    @After
    public void tearDown() {
        if (testUserId > 0) {
            userDAO.deleteUser(testUserId);  // Cascade deletes preferences
        }
        WeighToGoDBHelper.resetInstance();
    }

    /**
     * Helper method to create a test user.
     *
     * @param username the username for the test user
     * @return a User object with valid data
     */
    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash123");
        user.setSalt("salt123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        return user;
    }

    // Tests will be added here following strict TDD (one at a time)

    /**
     * Test 1: GET with non-existent key returns default value.
     *
     * Tests FR6.0.1 - UserPreferenceDAO generic key-value storage.
     * Verifies that getPreference() returns the provided default value
     * when the requested key doesn't exist in the database.
     */
    @Test
    public void test_getPreference_withNonExistentKey_returnsDefaultValue() {
        // ACT
        String result = userPreferenceDAO.getPreference(testUserId, "theme", "light");

        // ASSERT
        assertEquals("Should return default when key doesn't exist", "light", result);
    }
}
