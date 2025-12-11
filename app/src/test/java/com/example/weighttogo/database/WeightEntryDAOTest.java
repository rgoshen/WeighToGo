package com.example.weighttogo.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.example.weighttogo.models.User;
import com.example.weighttogo.models.WeightEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for WeightEntryDAO.
 * Tests all CRUD operations using Robolectric for in-memory database.
 */
@RunWith(RobolectricTestRunner.class)
public class WeightEntryDAOTest {

    private WeighToGoDBHelper dbHelper;
    private WeightEntryDAO weightEntryDAO;
    private UserDAO userDAO;
    private long testUserId;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        dbHelper = WeighToGoDBHelper.getInstance(context);
        weightEntryDAO = new WeightEntryDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);

        // Create a test user for foreign key relationships
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hash123");
        testUser.setSalt("salt123");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setIsActive(true);

        testUserId = userDAO.insertUser(testUser);
        assertTrue("Test user should be created", testUserId > 0);
    }

    @After
    public void tearDown() {
        // Clean up test data
        if (testUserId > 0) {
            userDAO.deleteUser(testUserId);
        }
        // Don't close dbHelper - it's a singleton and other tests may need it
    }

    @Test
    public void test_insertWeightEntry_withValidData_returnsWeightId() {
        // ARRANGE
        WeightEntry entry = new WeightEntry();
        entry.setUserId(testUserId);
        entry.setWeightValue(175.5);
        entry.setWeightUnit("lbs");
        entry.setWeightDate(LocalDate.now());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setIsDeleted(false);

        // ACT
        long weightId = weightEntryDAO.insertWeightEntry(entry);

        // ASSERT
        assertTrue("Weight ID should be greater than 0", weightId > 0);
    }

    @Test
    public void test_insertWeightEntry_withNotes_savesNotes() {
        // ARRANGE
        WeightEntry entry = new WeightEntry();
        entry.setUserId(testUserId);
        entry.setWeightValue(180.0);
        entry.setWeightUnit("lbs");
        entry.setWeightDate(LocalDate.now());
        entry.setNotes("Feeling great today!");
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setIsDeleted(false);

        // ACT
        long weightId = weightEntryDAO.insertWeightEntry(entry);
        WeightEntry retrieved = weightEntryDAO.getWeightEntryById(weightId);

        // ASSERT
        assertNotNull("Entry should be retrieved", retrieved);
        assertEquals("Notes should match", "Feeling great today!", retrieved.getNotes());
    }

    @Test
    public void test_getWeightEntryById_withExistingEntry_returnsEntry() {
        // ARRANGE
        WeightEntry entry = new WeightEntry();
        entry.setUserId(testUserId);
        entry.setWeightValue(160.5);
        entry.setWeightUnit("lbs");
        entry.setWeightDate(LocalDate.of(2025, 12, 10));
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setIsDeleted(false);

        long weightId = weightEntryDAO.insertWeightEntry(entry);

        // ACT
        WeightEntry retrieved = weightEntryDAO.getWeightEntryById(weightId);

        // ASSERT
        assertNotNull("Entry should be found", retrieved);
        assertEquals("Weight ID should match", weightId, retrieved.getWeightId());
        assertEquals("Weight value should match", 160.5, retrieved.getWeightValue(), 0.01);
        assertEquals("Weight unit should match", "lbs", retrieved.getWeightUnit());
        assertEquals("Weight date should match", LocalDate.of(2025, 12, 10), retrieved.getWeightDate());
    }

    @Test
    public void test_getWeightEntryById_withNonExistentEntry_returnsNull() {
        // ACT
        WeightEntry retrieved = weightEntryDAO.getWeightEntryById(99999);

        // ASSERT
        assertNull("Non-existent entry should return null", retrieved);
    }

    @Test
    public void test_getWeightEntriesForUser_withMultipleEntries_returnsAllNonDeleted() {
        // ARRANGE
        WeightEntry entry1 = createTestEntry(testUserId, 170.0, LocalDate.of(2025, 12, 8), false);
        WeightEntry entry2 = createTestEntry(testUserId, 171.0, LocalDate.of(2025, 12, 9), false);
        WeightEntry entry3 = createTestEntry(testUserId, 172.0, LocalDate.of(2025, 12, 10), true); // Deleted

        weightEntryDAO.insertWeightEntry(entry1);
        weightEntryDAO.insertWeightEntry(entry2);
        weightEntryDAO.insertWeightEntry(entry3);

        // ACT
        List<WeightEntry> entries = weightEntryDAO.getWeightEntriesForUser(testUserId);

        // ASSERT
        assertEquals("Should return 2 non-deleted entries", 2, entries.size());
        // Entries should be ordered by date DESC (most recent first)
        assertEquals("First entry should be most recent", LocalDate.of(2025, 12, 9), entries.get(0).getWeightDate());
        assertEquals("Second entry should be older", LocalDate.of(2025, 12, 8), entries.get(1).getWeightDate());
    }

    @Test
    public void test_getWeightEntriesForUser_withNoEntries_returnsEmptyList() {
        // Create a second user with no entries
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPasswordHash("hash");
        user2.setSalt("salt");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setIsActive(true);
        long user2Id = userDAO.insertUser(user2);

        // ACT
        List<WeightEntry> entries = weightEntryDAO.getWeightEntriesForUser(user2Id);

        // ASSERT
        assertNotNull("List should not be null", entries);
        assertEquals("List should be empty", 0, entries.size());
    }

    @Test
    public void test_getLatestWeightEntry_withMultipleEntries_returnsMostRecent() {
        // ARRANGE
        WeightEntry entry1 = createTestEntry(testUserId, 170.0, LocalDate.of(2025, 12, 8), false);
        WeightEntry entry2 = createTestEntry(testUserId, 171.0, LocalDate.of(2025, 12, 9), false);
        WeightEntry entry3 = createTestEntry(testUserId, 172.0, LocalDate.of(2025, 12, 10), false);

        weightEntryDAO.insertWeightEntry(entry1);
        weightEntryDAO.insertWeightEntry(entry2);
        weightEntryDAO.insertWeightEntry(entry3);

        // ACT
        WeightEntry latest = weightEntryDAO.getLatestWeightEntry(testUserId);

        // ASSERT
        assertNotNull("Latest entry should be found", latest);
        assertEquals("Latest entry should have most recent date", LocalDate.of(2025, 12, 10), latest.getWeightDate());
        assertEquals("Latest entry should have correct weight", 172.0, latest.getWeightValue(), 0.01);
    }

    @Test
    public void test_getLatestWeightEntry_withNoEntries_returnsNull() {
        // Create a second user with no entries
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPasswordHash("hash");
        user2.setSalt("salt");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setIsActive(true);
        long user2Id = userDAO.insertUser(user2);

        // ACT
        WeightEntry latest = weightEntryDAO.getLatestWeightEntry(user2Id);

        // ASSERT
        assertNull("Latest entry should be null for user with no entries", latest);
    }

    @Test
    public void test_updateWeightEntry_withValidData_updatesEntry() {
        // ARRANGE
        WeightEntry entry = createTestEntry(testUserId, 175.0, LocalDate.now(), false);
        long weightId = weightEntryDAO.insertWeightEntry(entry);

        entry.setWeightId(weightId);
        entry.setWeightValue(176.5);
        entry.setNotes("Updated notes");

        // ACT
        int rowsUpdated = weightEntryDAO.updateWeightEntry(entry);
        WeightEntry retrieved = weightEntryDAO.getWeightEntryById(weightId);

        // ASSERT
        assertEquals("Should update 1 row", 1, rowsUpdated);
        assertNotNull("Entry should still exist", retrieved);
        assertEquals("Weight value should be updated", 176.5, retrieved.getWeightValue(), 0.01);
        assertEquals("Notes should be updated", "Updated notes", retrieved.getNotes());
    }

    @Test
    public void test_deleteWeightEntry_softDeletes_setsIsDeletedFlag() {
        // ARRANGE
        WeightEntry entry = createTestEntry(testUserId, 175.0, LocalDate.now(), false);
        long weightId = weightEntryDAO.insertWeightEntry(entry);

        // ACT
        int rowsDeleted = weightEntryDAO.deleteWeightEntry(weightId);
        WeightEntry retrieved = weightEntryDAO.getWeightEntryById(weightId);
        List<WeightEntry> userEntries = weightEntryDAO.getWeightEntriesForUser(testUserId);

        // ASSERT
        assertEquals("Should update 1 row", 1, rowsDeleted);
        assertNotNull("Entry should still exist in database", retrieved);
        assertTrue("Entry should be marked as deleted", retrieved.getIsDeleted());
        assertEquals("Deleted entry should not appear in user's list", 0, userEntries.size());
    }

    // Helper method to create test entries
    private WeightEntry createTestEntry(long userId, double weight, LocalDate date, boolean isDeleted) {
        WeightEntry entry = new WeightEntry();
        entry.setUserId(userId);
        entry.setWeightValue(weight);
        entry.setWeightUnit("lbs");
        entry.setWeightDate(date);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setIsDeleted(isDeleted);
        return entry;
    }
}
