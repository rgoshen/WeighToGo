package com.example.weighttogo.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.weighttogo.R;
import com.example.weighttogo.database.DatabaseException;
import com.example.weighttogo.database.GoalWeightDAO;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.database.WeightEntryDAO;
import com.example.weighttogo.models.GoalWeight;
import com.example.weighttogo.models.User;
import com.example.weighttogo.models.WeightEntry;
import com.example.weighttogo.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * Integration tests for MainActivity.
 * Tests dashboard functionality, authentication, data loading, and user interactions.
 *
 * **Phase 8A Refactoring**: Converted to use Mockito mocks instead of real database.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class MainActivityTest {

    @Mock private UserDAO mockUserDAO;
    @Mock private WeightEntryDAO mockWeightEntryDAO;
    @Mock private GoalWeightDAO mockGoalWeightDAO;
    @Mock private SessionManager mockSessionManager;
    @Mock private WeighToGoDBHelper mockDbHelper;

    private MainActivity activity;
    private long testUserId;
    private User testUser;
    private AtomicLong idGenerator;

    @Before
    public void setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this);

        // Initialize ID generator for predictable, thread-safe mock IDs
        idGenerator = new AtomicLong(1);

        // Create test user data
        testUserId = 1L;
        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashed_password");
        testUser.setSalt("test_salt");
        testUser.setPasswordAlgorithm("SHA256");
        testUser.setDisplayName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setActive(true);

        // Set default mock behaviors
        when(mockSessionManager.isLoggedIn()).thenReturn(false);
        when(mockSessionManager.getCurrentUserId()).thenReturn(0L);

        // Stub helper method DAO calls to return realistic values
        when(mockWeightEntryDAO.insertWeightEntry(any(WeightEntry.class)))
                .thenAnswer(invocation -> idGenerator.getAndIncrement());

        when(mockGoalWeightDAO.insertGoal(any(GoalWeight.class)))
                .thenAnswer(invocation -> {
                    return 1L; // Return valid goal ID
                });

        when(mockUserDAO.getUserById(testUserId)).thenReturn(testUser);
    }

    @After
    public void tearDown() {
        if (activity != null) {
            activity.finish();
        }
    }

    /**
     * Test 1: onCreate when not logged in redirects to LoginActivity
     * NOTE: Also affected by Robolectric/Material3 theme issue (GH #12)
     * Will be migrated to Espresso with tests 2-18 in Phase 8.4
     *
     * **Phase 8A Refactoring**: Now uses Mockito mocks via setter injection.
     */
    @Ignore("Robolectric/Material3 theme incompatibility - migrate to Espresso (GH #12)")
    @Test
    public void test_onCreate_whenNotLoggedIn_redirectsToLogin() {
        // ARRANGE - Stub mock to return false for isLoggedIn()
        when(mockSessionManager.isLoggedIn()).thenReturn(false);

        // ACT - Build activity, inject mocks BEFORE create
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
        activity = controller.get();

        // Inject mocks BEFORE calling create()
        activity.setUserDAO(mockUserDAO);
        activity.setWeightEntryDAO(mockWeightEntryDAO);
        activity.setGoalWeightDAO(mockGoalWeightDAO);
        activity.setSessionManager(mockSessionManager);
        activity.setDbHelper(mockDbHelper);

        // NOW call create() on the same instance
        controller.create();

        // ASSERT - Use the same activity instance
        Intent expectedIntent = new Intent(activity, LoginActivity.class);
        Intent actualIntent = shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity();
        assertNotNull("Should start LoginActivity", actualIntent);
        assertEquals("Should redirect to LoginActivity",
                expectedIntent.getComponent(), actualIntent.getComponent());
        assertTrue("MainActivity should finish", activity.isFinishing());

        // Verify mock was called
        verify(mockSessionManager).isLoggedIn();
    }

    // ============================================================
    // TESTS 2-18 MIGRATED TO ESPRESSO (Phase 8B Complete)
    // ============================================================
    // Issue: GH #12 - Robolectric/Material3 theme incompatibility - RESOLVED
    // Resolution: Tests migrated to Espresso instrumented tests
    // Location: app/src/androidTest/java/com/example/weighttogo/activities/MainActivityEspressoTest.java
    //
    // All 17 tests now run on real Android device/emulator with full Material3 theme support.
    // Tests cover: UI init, empty state, RecyclerView, progress card, quick stats,
    // delete flow, navigation, user info, and progress calculations.
    // ============================================================

    // Former Tests 2-18 have been successfully migrated to MainActivityEspressoTest.java
    // No commented code remains - see Espresso test file for implementation details

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Creates a test weight entry for the test user
     */
    private long createTestWeightEntry(double weight) {
        return createTestWeightEntryOnDate(weight, LocalDate.now());
    }

    /**
     * Creates a test weight entry for a specific date with proper mock stubbing.
     *
     * NOTE: Mock stubs configured for Phase 8B Espresso migration.
     * May need adjustment when tests are un-ignored.
     */
    private long createTestWeightEntryOnDate(double weight, LocalDate date) {
        WeightEntry entry = new WeightEntry();
        entry.setUserId(testUserId);
        entry.setWeightValue(weight);
        entry.setWeightUnit("lbs");
        entry.setWeightDate(date);
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setDeleted(false);

        // Generate unique ID and configure mock stubs
        long entryId = idGenerator.getAndIncrement();
        entry.setWeightId(entryId);

        when(mockWeightEntryDAO.insertWeightEntry(entry)).thenReturn(entryId);
        when(mockWeightEntryDAO.getWeightEntryById(entryId)).thenReturn(entry);
        when(mockWeightEntryDAO.getLatestWeightEntry(testUserId)).thenReturn(entry);

        return mockWeightEntryDAO.insertWeightEntry(entry);
    }

    /**
     * Creates a test goal for the test user with proper mock stubbing.
     *
     * NOTE: Mock stubs configured for Phase 8B Espresso migration.
     * May need adjustment when tests are un-ignored.
     */
    private void createTestGoal(double startWeight, double goalWeight) {
        GoalWeight goal = new GoalWeight();
        goal.setUserId(testUserId);
        goal.setGoalWeight(goalWeight);
        goal.setGoalUnit("lbs");
        goal.setStartWeight(startWeight);
        goal.setActive(true);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        // Generate unique ID and configure mock stubs
        long goalId = idGenerator.getAndIncrement();
        goal.setGoalId(goalId);

        when(mockGoalWeightDAO.insertGoal(goal)).thenReturn(goalId);
        when(mockGoalWeightDAO.getActiveGoal(testUserId)).thenReturn(goal);
        when(mockGoalWeightDAO.getGoalById(goalId)).thenReturn(goal);

        mockGoalWeightDAO.insertGoal(goal);
    }
}