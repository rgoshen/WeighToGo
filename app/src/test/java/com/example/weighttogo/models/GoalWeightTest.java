package com.example.weighttogo.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for GoalWeight model class.
 * Tests FR1.2 - Goal Weight data model.
 */
public class GoalWeightTest {

    @Test
    public void test_defaultConstructor_createsGoalWeightObject() {
        // ARRANGE & ACT
        GoalWeight goal = new GoalWeight();

        // ASSERT
        assertNotNull("GoalWeight object should not be null", goal);
    }

    @Test
    public void test_getGoalId_defaultValue_returnsZero() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();

        // ACT
        long goalId = goal.getGoalId();

        // ASSERT
        assertEquals("Default goalId should be 0", 0L, goalId);
    }

    @Test
    public void test_setGoalId_withValidId_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        long expectedId = 42L;

        // ACT
        goal.setGoalId(expectedId);

        // ASSERT
        assertEquals("GoalId should be set correctly", expectedId, goal.getGoalId());
    }

    @Test
    public void test_setUserId_withValidId_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        long expectedUserId = 123L;

        // ACT
        goal.setUserId(expectedUserId);

        // ASSERT
        assertEquals("UserId should be set correctly", expectedUserId, goal.getUserId());
    }

    @Test
    public void test_setGoalWeight_withValidValue_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        double expectedGoalWeight = 150.0;

        // ACT
        goal.setGoalWeight(expectedGoalWeight);

        // ASSERT
        assertEquals("GoalWeight should be set correctly", expectedGoalWeight, goal.getGoalWeight(), 0.01);
    }

    @Test
    public void test_setGoalUnit_withValidUnit_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        String expectedUnit = "lbs";

        // ACT
        goal.setGoalUnit(expectedUnit);

        // ASSERT
        assertEquals("GoalUnit should be set correctly", expectedUnit, goal.getGoalUnit());
    }

    @Test
    public void test_setStartWeight_withValidValue_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        double expectedStartWeight = 200.0;

        // ACT
        goal.setStartWeight(expectedStartWeight);

        // ASSERT
        assertEquals("StartWeight should be set correctly", expectedStartWeight, goal.getStartWeight(), 0.01);
    }

    @Test
    public void test_setTargetDate_withValidDate_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        String expectedDate = "2025-12-31";

        // ACT
        goal.setTargetDate(expectedDate);

        // ASSERT
        assertEquals("TargetDate should be set correctly", expectedDate, goal.getTargetDate());
    }

    @Test
    public void test_setIsAchieved_withValidFlag_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        int expectedFlag = 1;

        // ACT
        goal.setIsAchieved(expectedFlag);

        // ASSERT
        assertEquals("IsAchieved should be set correctly", expectedFlag, goal.getIsAchieved());
    }

    @Test
    public void test_setAchievedDate_withValidDate_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        String expectedDate = "2025-12-10";

        // ACT
        goal.setAchievedDate(expectedDate);

        // ASSERT
        assertEquals("AchievedDate should be set correctly", expectedDate, goal.getAchievedDate());
    }

    @Test
    public void test_setCreatedAt_withValidTimestamp_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        String expectedTimestamp = "2025-12-10 08:00:00";

        // ACT
        goal.setCreatedAt(expectedTimestamp);

        // ASSERT
        assertEquals("CreatedAt should be set correctly", expectedTimestamp, goal.getCreatedAt());
    }

    @Test
    public void test_setUpdatedAt_withValidTimestamp_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        String expectedTimestamp = "2025-12-10 09:00:00";

        // ACT
        goal.setUpdatedAt(expectedTimestamp);

        // ASSERT
        assertEquals("UpdatedAt should be set correctly", expectedTimestamp, goal.getUpdatedAt());
    }

    @Test
    public void test_setIsActive_withValidFlag_setsValue() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        int expectedFlag = 1;

        // ACT
        goal.setIsActive(expectedFlag);

        // ASSERT
        assertEquals("IsActive should be set correctly", expectedFlag, goal.getIsActive());
    }

    @Test
    public void test_toString_returnsNonNullString() {
        // ARRANGE
        GoalWeight goal = new GoalWeight();
        goal.setGoalId(1L);
        goal.setUserId(123L);
        goal.setGoalWeight(150.0);
        goal.setGoalUnit("lbs");
        goal.setIsActive(1);

        // ACT
        String result = goal.toString();

        // ASSERT
        assertNotNull("toString should return non-null value", result);
        assertTrue("toString should contain goalId", result.contains("1"));
        assertTrue("toString should contain userId", result.contains("123"));
        assertTrue("toString should contain goalWeight", result.contains("150.0"));
        assertTrue("toString should contain goalUnit", result.contains("lbs"));
    }
}
