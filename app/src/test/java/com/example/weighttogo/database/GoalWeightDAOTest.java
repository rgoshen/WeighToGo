package com.example.weighttogo.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.example.weighttogo.models.GoalWeight;
import com.example.weighttogo.models.User;

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
 * Unit tests for GoalWeightDAO.
 * Tests all CRUD operations using Robolectric for in-memory database.
 */
@RunWith(RobolectricTestRunner.class)
public class GoalWeightDAOTest {

    private WeighToGoDBHelper dbHelper;
    private GoalWeightDAO goalWeightDAO;
    private UserDAO userDAO;
    private long testUserId;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        dbHelper = WeighToGoDBHelper.getInstance(context);
        goalWeightDAO = new GoalWeightDAO(dbHelper);
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
    public void test_insertGoal_withValidData_returnsGoalId() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        goal.setUserId(testUserId);
        goal.setGoalWeight(150.0);
        goal.setGoalUnit("lbs");
        goal.setStartWeight(180.0);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setIsActive(true);
        goal.setIsAchieved(false);

        // ACT
        long goalId = goalWeightDAO.insertGoal(goal);

        // ASSERT
        assertTrue("Goal ID should be greater than 0", goalId > 0);
    }

    @Test
    public void test_insertGoal_withTargetDate_savesTargetDate() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        goal.setUserId(testUserId);
        goal.setGoalWeight(150.0);
        goal.setGoalUnit("lbs");
        goal.setStartWeight(180.0);
        goal.setTargetDate(LocalDate.of(2026, 6, 1));
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setIsActive(true);
        goal.setIsAchieved(false);

        // ACT
        long goalId = goalWeightDAO.insertGoal(goal);
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertNotNull("Active goal should be retrieved", activeGoal);
        assertEquals("Target date should match", LocalDate.of(2026, 6, 1), activeGoal.getTargetDate());
    }

    @Test
    public void test_getActiveGoal_withActiveGoal_returnsGoal() {
        // ARRANGE
        GoalWeight goal = createTestGoal(testUserId, 150.0, 180.0, true, false);
        goalWeightDAO.insertGoal(goal);

        // ACT
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertNotNull("Active goal should be found", activeGoal);
        assertEquals("Goal weight should match", 150.0, activeGoal.getGoalWeight(), 0.01);
        assertEquals("Start weight should match", 180.0, activeGoal.getStartWeight(), 0.01);
        assertTrue("Goal should be active", activeGoal.getIsActive());
        assertFalse("Goal should not be achieved", activeGoal.getIsAchieved());
    }

    @Test
    public void test_getActiveGoal_withNoActiveGoal_returnsNull() {
        // ARRANGE
        GoalWeight inactiveGoal = createTestGoal(testUserId, 150.0, 180.0, false, false);
        goalWeightDAO.insertGoal(inactiveGoal);

        // ACT
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertNull("Should return null when no active goal exists", activeGoal);
    }

    @Test
    public void test_getActiveGoal_withMultipleGoals_returnsMostRecent() {
        try {
            // ARRANGE - Insert two active goals (edge case)
            GoalWeight goal1 = createTestGoal(testUserId, 150.0, 180.0, true, false);
            GoalWeight goal2 = createTestGoal(testUserId, 145.0, 175.0, true, false);

            Thread.sleep(10); // Ensure different created_at timestamps
            goalWeightDAO.insertGoal(goal1);
            Thread.sleep(10);
            goalWeightDAO.insertGoal(goal2);

            // ACT
            GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

            // ASSERT
            assertNotNull("Active goal should be found", activeGoal);
            assertEquals("Should return most recent active goal", 145.0, activeGoal.getGoalWeight(), 0.01);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_getGoalHistory_withMultipleGoals_returnsAllGoals() {
        // ARRANGE
        GoalWeight goal1 = createTestGoal(testUserId, 150.0, 180.0, false, true);
        GoalWeight goal2 = createTestGoal(testUserId, 145.0, 175.0, true, false);
        GoalWeight goal3 = createTestGoal(testUserId, 140.0, 170.0, false, false);

        goalWeightDAO.insertGoal(goal1);
        goalWeightDAO.insertGoal(goal2);
        goalWeightDAO.insertGoal(goal3);

        // ACT
        List<GoalWeight> history = goalWeightDAO.getGoalHistory(testUserId);

        // ASSERT
        assertEquals("Should return all 3 goals", 3, history.size());
        // Should be ordered by created_at DESC (most recent first)
    }

    @Test
    public void test_getGoalHistory_withNoGoals_returnsEmptyList() {
        // Create a second user with no goals
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPasswordHash("hash");
        user2.setSalt("salt");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setIsActive(true);
        long user2Id = userDAO.insertUser(user2);

        // ACT
        List<GoalWeight> history = goalWeightDAO.getGoalHistory(user2Id);

        // ASSERT
        assertNotNull("List should not be null", history);
        assertEquals("List should be empty", 0, history.size());
    }

    @Test
    public void test_updateGoal_withValidData_updatesGoal() {
        // ARRANGE
        GoalWeight goal = createTestGoal(testUserId, 150.0, 180.0, true, false);
        long goalId = goalWeightDAO.insertGoal(goal);

        goal.setGoalId(goalId);
        goal.setGoalWeight(145.0);
        goal.setIsAchieved(true);
        goal.setAchievedDate(LocalDate.now());

        // ACT
        int rowsUpdated = goalWeightDAO.updateGoal(goal);
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertEquals("Should update 1 row", 1, rowsUpdated);
        assertNotNull("Goal should still exist", activeGoal);
        assertEquals("Goal weight should be updated", 145.0, activeGoal.getGoalWeight(), 0.01);
        assertTrue("Goal should be marked as achieved", activeGoal.getIsAchieved());
        assertNotNull("Achieved date should be set", activeGoal.getAchievedDate());
    }

    @Test
    public void test_deactivateGoal_setsIsActiveFalse() {
        // ARRANGE
        GoalWeight goal = createTestGoal(testUserId, 150.0, 180.0, true, false);
        long goalId = goalWeightDAO.insertGoal(goal);

        // ACT
        int rowsUpdated = goalWeightDAO.deactivateGoal(goalId);
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertEquals("Should update 1 row", 1, rowsUpdated);
        assertNull("Should have no active goal after deactivation", activeGoal);
    }

    @Test
    public void test_deactivateAllGoalsForUser_deactivatesMultipleGoals() {
        // ARRANGE
        GoalWeight goal1 = createTestGoal(testUserId, 150.0, 180.0, true, false);
        GoalWeight goal2 = createTestGoal(testUserId, 145.0, 175.0, true, false);
        GoalWeight goal3 = createTestGoal(testUserId, 140.0, 170.0, false, false); // Already inactive

        goalWeightDAO.insertGoal(goal1);
        goalWeightDAO.insertGoal(goal2);
        goalWeightDAO.insertGoal(goal3);

        // ACT
        int rowsUpdated = goalWeightDAO.deactivateAllGoalsForUser(testUserId);
        GoalWeight activeGoal = goalWeightDAO.getActiveGoal(testUserId);

        // ASSERT
        assertEquals("Should update 2 rows (2 active goals)", 2, rowsUpdated);
        assertNull("Should have no active goal after deactivation", activeGoal);
    }

    @Test
    public void test_deactivateAllGoalsForUser_withNoActiveGoals_updatesZeroRows() {
        // ARRANGE
        GoalWeight inactiveGoal = createTestGoal(testUserId, 150.0, 180.0, false, false);
        goalWeightDAO.insertGoal(inactiveGoal);

        // ACT
        int rowsUpdated = goalWeightDAO.deactivateAllGoalsForUser(testUserId);

        // ASSERT
        assertEquals("Should update 0 rows when no active goals exist", 0, rowsUpdated);
    }

    // Helper method to create test goals
    private GoalWeight createTestGoal(long userId, double goalWeight, double startWeight, boolean isActive, boolean isAchieved) {
        GoalWeight goal = new GoalWeight();
        goal.setUserId(userId);
        goal.setGoalWeight(goalWeight);
        goal.setGoalUnit("lbs");
        goal.setStartWeight(startWeight);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setIsActive(isActive);
        goal.setIsAchieved(isAchieved);
        return goal;
    }
}
