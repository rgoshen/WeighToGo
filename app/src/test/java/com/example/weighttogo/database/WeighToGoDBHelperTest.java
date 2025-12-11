package com.example.weighttogo.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

/**
 * Unit tests for WeighToGoDBHelper database helper class.
 * Uses Robolectric for Android framework components (SQLite).
 *
 * Following strict TDD: one failing test at a time.
 */
@RunWith(RobolectricTestRunner.class)
public class WeighToGoDBHelperTest {

    private Context context;
    private WeighToGoDBHelper dbHelper;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        // Get fresh instance for each test
        dbHelper = WeighToGoDBHelper.getInstance(context);
    }

    @After
    public void tearDown() {
        try {
            if (dbHelper != null) {
                dbHelper.close();
            }
        } finally {
            dbHelper = null;  // Explicit null assignment to prevent accidental reuse
            // Clean up database file (guaranteed to run even if close() fails)
            context.deleteDatabase("weigh_to_go.db");

            // Reset singleton instance for test isolation
            // This ensures each test gets a fresh database instance
            WeighToGoDBHelper.resetInstance();
        }
    }

    /**
     * Test 1: getInstance returns a non-null instance
     */
    @Test
    public void test_getInstance_returnsSingletonInstance() {
        // ACT
        WeighToGoDBHelper instance = WeighToGoDBHelper.getInstance(context);

        // ASSERT
        assertNotNull("getInstance should return non-null WeighToGoDBHelper", instance);
    }

    /**
     * Test 2: getInstance called twice returns the same instance (Singleton pattern)
     */
    @Test
    public void test_getInstance_calledTwice_returnsSameInstance() {
        // ACT
        WeighToGoDBHelper instance1 = WeighToGoDBHelper.getInstance(context);
        WeighToGoDBHelper instance2 = WeighToGoDBHelper.getInstance(context);

        // ASSERT
        assertSame("getInstance should return the same instance (Singleton)", instance1, instance2);
    }

    /**
     * Test 3: onCreate creates users table with correct schema
     */
    @Test
    public void test_onCreate_createsUsersTable() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check table exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='users'",
            null
        )) {
            assertTrue("users table should exist", cursor.moveToFirst());
        }

        // ASSERT - Check table schema
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(users)", null)) {
            int columnCount = cursor.getCount();
            assertEquals("users table should have 11 columns", 11, columnCount);

            // Verify column names (order matters in PRAGMA table_info)
            String[] expectedColumns = {"id", "username", "password_hash", "salt", "created_at", "last_login", "email", "phone_number", "display_name", "updated_at", "is_active"};
            int index = 0;
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                assertEquals("Column " + index + " should be " + expectedColumns[index],
                    expectedColumns[index], columnName);
                index++;
            }
        }
    }

    /**
     * Test 4: onCreate creates weight_entries table with correct schema
     */
    @Test
    public void test_onCreate_createsWeightEntriesTable() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check table exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='weight_entries'",
            null
        )) {
            assertTrue("weight_entries table should exist", cursor.moveToFirst());
        }

        // ASSERT - Check table schema
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(weight_entries)", null)) {
            int columnCount = cursor.getCount();
            assertEquals("weight_entries table should have 9 columns", 9, columnCount);

            // Verify required columns exist
            boolean hasWeightId = false;
            boolean hasUserId = false;
            boolean hasWeightValue = false;
            boolean hasWeightDate = false;
            boolean hasIsDeleted = false;

            while (cursor.moveToNext()) {
                String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equals("id")) hasWeightId = true;
                if (columnName.equals("user_id")) hasUserId = true;
                if (columnName.equals("weight_value")) hasWeightValue = true;
                if (columnName.equals("weight_date")) hasWeightDate = true;
                if (columnName.equals("is_deleted")) hasIsDeleted = true;
            }

            assertTrue("weight_entries should have id column", hasWeightId);
            assertTrue("weight_entries should have user_id column", hasUserId);
            assertTrue("weight_entries should have weight_value column", hasWeightValue);
            assertTrue("weight_entries should have weight_date column", hasWeightDate);
            assertTrue("weight_entries should have is_deleted column", hasIsDeleted);
        }
    }

    /**
     * Test 5: onCreate creates goal_weights table with correct schema
     */
    @Test
    public void test_onCreate_createsGoalWeightsTable() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check table exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='goal_weights'",
            null
        )) {
            assertTrue("goal_weights table should exist", cursor.moveToFirst());
        }

        // ASSERT - Check table schema
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(goal_weights)", null)) {
            int columnCount = cursor.getCount();
            assertEquals("goal_weights table should have 11 columns", 11, columnCount);

            // Verify required columns exist
            boolean hasGoalId = false;
            boolean hasUserId = false;
            boolean hasGoalWeight = false;
            boolean hasIsActive = false;

            while (cursor.moveToNext()) {
                String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equals("id")) hasGoalId = true;
                if (columnName.equals("user_id")) hasUserId = true;
                if (columnName.equals("goal_weight")) hasGoalWeight = true;
                if (columnName.equals("is_active")) hasIsActive = true;
            }

            assertTrue("goal_weights should have id column", hasGoalId);
            assertTrue("goal_weights should have user_id column", hasUserId);
            assertTrue("goal_weights should have goal_weight column", hasGoalWeight);
            assertTrue("goal_weights should have is_active column", hasIsActive);
        }
    }

    /**
     * Test 6: onConfigure enables foreign keys
     */
    @Test
    public void test_onConfigure_enablesForeignKeys() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check foreign_keys pragma is ON
        try (Cursor cursor = db.rawQuery("PRAGMA foreign_keys", null)) {
            assertTrue("PRAGMA foreign_keys should return a result", cursor.moveToFirst());
            int foreignKeysEnabled = cursor.getInt(0);
            assertEquals("Foreign keys should be enabled (1)", 1, foreignKeysEnabled);
        }
    }

    // ========== EDGE CASE TESTS ==========

    /**
     * Test 7: Foreign key constraint prevents orphaned weight entries
     * Attempting to insert weight_entry with non-existent user_id should fail
     */
    @Test(expected = android.database.sqlite.SQLiteConstraintException.class)
    public void test_foreignKey_preventOrphanedRecords() {
        // ARRANGE
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int nonExistentUserId = 999;

        // ACT - Try to insert weight_entry with invalid user_id
        // This should throw SQLiteConstraintException due to FOREIGN KEY constraint
        db.execSQL(
            "INSERT INTO weight_entries (user_id, weight_value, weight_unit, weight_date, created_at, updated_at, is_deleted) " +
            "VALUES (" + nonExistentUserId + ", 175.5, 'lbs', '2025-12-10', '2025-12-10 10:00:00', '2025-12-10 10:00:00', 0)"
        );

        // ASSERT - Exception should be thrown (test passes if exception is thrown)
    }

    /**
     * Test 8: Foreign key CASCADE DELETE removes child records when parent is deleted
     * Deleting a user should automatically delete their weight_entries and goal_weights
     */
    @Test
    public void test_foreignKey_cascadeDelete() {
        // ARRANGE
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert test user
        db.execSQL(
            "INSERT INTO users (username, password_hash, salt, created_at, updated_at, is_active) " +
            "VALUES ('testuser', 'hash123', 'salt456', '2025-12-10 10:00:00', '2025-12-10 10:00:00', 1)"
        );

        // Get the inserted user ID
        Cursor userCursor = db.rawQuery("SELECT id FROM users WHERE username = 'testuser'", null);
        assertTrue("User should be inserted", userCursor.moveToFirst());
        int userId = userCursor.getInt(0);
        userCursor.close();

        // Insert weight_entry for this user
        db.execSQL(
            "INSERT INTO weight_entries (user_id, weight_value, weight_unit, weight_date, created_at, updated_at, is_deleted) " +
            "VALUES (" + userId + ", 175.5, 'lbs', '2025-12-10', '2025-12-10 10:00:00', '2025-12-10 10:00:00', 0)"
        );

        // Insert goal_weight for this user
        db.execSQL(
            "INSERT INTO goal_weights (user_id, goal_weight, goal_unit, start_weight, created_at, updated_at, is_active) " +
            "VALUES (" + userId + ", 160.0, 'lbs', 175.5, '2025-12-10 10:00:00', '2025-12-10 10:00:00', 1)"
        );

        // Verify records exist before deletion
        Cursor entryCursor = db.rawQuery("SELECT COUNT(*) FROM weight_entries WHERE user_id = " + userId, null);
        entryCursor.moveToFirst();
        assertEquals("Should have 1 weight entry", 1, entryCursor.getInt(0));
        entryCursor.close();

        Cursor goalCursor = db.rawQuery("SELECT COUNT(*) FROM goal_weights WHERE user_id = " + userId, null);
        goalCursor.moveToFirst();
        assertEquals("Should have 1 goal weight", 1, goalCursor.getInt(0));
        goalCursor.close();

        // ACT - Delete the user
        db.execSQL("DELETE FROM users WHERE id = " + userId);

        // ASSERT - Child records should be automatically deleted via CASCADE DELETE
        entryCursor = db.rawQuery("SELECT COUNT(*) FROM weight_entries WHERE user_id = " + userId, null);
        entryCursor.moveToFirst();
        assertEquals("Weight entries should be cascade deleted", 0, entryCursor.getInt(0));
        entryCursor.close();

        goalCursor = db.rawQuery("SELECT COUNT(*) FROM goal_weights WHERE user_id = " + userId, null);
        goalCursor.moveToFirst();
        assertEquals("Goal weights should be cascade deleted", 0, goalCursor.getInt(0));
        goalCursor.close();
    }

    /**
     * Test 9: onCreate creates index on weight_entries.user_id
     * Foreign key columns should have indexes for query performance
     */
    @Test
    public void test_onCreate_createsIndexOnWeightEntriesUserId() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_weight_entries_user_id'",
            null
        )) {
            assertTrue("Index idx_weight_entries_user_id should exist", cursor.moveToFirst());
        }
    }

    /**
     * Test 10: onCreate creates index on goal_weights.user_id
     * Foreign key columns should have indexes for query performance
     */
    @Test
    public void test_onCreate_createsIndexOnGoalWeightsUserId() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_goal_weights_user_id'",
            null
        )) {
            assertTrue("Index idx_goal_weights_user_id should exist", cursor.moveToFirst());
        }
    }

    /**
     * Test 11: onCreate creates unique index on users.username
     * Username must be unique and queries should be fast
     */
    @Test
    public void test_onCreate_createsUniqueIndexOnUsername() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_users_username'",
            null
        )) {
            assertTrue("Index idx_users_username should exist", cursor.moveToFirst());
        }

        // ASSERT - Verify it's a unique index by checking sql column
        try (Cursor cursor = db.rawQuery(
            "SELECT sql FROM sqlite_master WHERE type='index' AND name='idx_users_username'",
            null
        )) {
            assertTrue("Should find index definition", cursor.moveToFirst());
            String sql = cursor.getString(0);
            assertTrue("Index should be UNIQUE", sql.toUpperCase().contains("UNIQUE"));
        }
    }

    /**
     * Test 12: onCreate creates index on weight_entries.weight_date
     * Date-based queries (recent entries, date ranges) should be optimized
     */
    @Test
    public void test_onCreate_createsIndexOnWeightDate() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_weight_entries_weight_date'",
            null
        )) {
            assertTrue("Index idx_weight_entries_weight_date should exist", cursor.moveToFirst());
        }
    }

    /**
     * Test 13: onCreate creates index on goal_weights.is_active
     * Finding active goal (common query) should be optimized
     */
    @Test
    public void test_onCreate_createsIndexOnGoalIsActive() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_goal_weights_is_active'",
            null
        )) {
            assertTrue("Index idx_goal_weights_is_active should exist", cursor.moveToFirst());
        }
    }

    /**
     * Test 14: onCreate creates index on weight_entries.is_deleted
     * Soft delete queries (WHERE is_deleted = 0) should be optimized
     */
    @Test
    public void test_onCreate_createsIndexOnIsDeleted() {
        // ACT
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ASSERT - Check index exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='index' AND name='idx_weight_entries_is_deleted'",
            null
        )) {
            assertTrue("Index idx_weight_entries_is_deleted should exist", cursor.moveToFirst());
        }
    }

    // ========== EDGE CASE TESTS ==========

    /**
     * Test 15: onUpgrade drops and recreates all tables
     * Simulates database upgrade scenario
     */
    @Test
    public void test_onUpgrade_dropsAndRecreatesTables() {
        // ARRANGE
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert test data
        db.execSQL(
            "INSERT INTO users (username, password_hash, salt, created_at, updated_at, is_active) " +
            "VALUES ('testuser', 'hash123', 'salt456', '2025-12-10 10:00:00', '2025-12-10 10:00:00', 1)"
        );

        // Verify data exists
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null)) {
            cursor.moveToFirst();
            int userCountBefore = cursor.getInt(0);
            assertEquals("Should have 1 user before upgrade", 1, userCountBefore);
        }

        // ACT - Trigger onUpgrade (simulates version 1 -> 2)
        dbHelper.onUpgrade(db, 1, 2);

        // ASSERT - Tables should be recreated (data lost, but tables exist)
        // Check users table exists and is empty
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null)) {
            cursor.moveToFirst();
            int userCountAfter = cursor.getInt(0);
            assertEquals("Users table should be empty after upgrade", 0, userCountAfter);
        }

        // Check weight_entries table exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='weight_entries'",
            null
        )) {
            assertTrue("weight_entries table should exist after upgrade", cursor.moveToFirst());
        }

        // Check goal_weights table exists
        try (Cursor cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='goal_weights'",
            null
        )) {
            assertTrue("goal_weights table should exist after upgrade", cursor.moveToFirst());
        }
    }
}
