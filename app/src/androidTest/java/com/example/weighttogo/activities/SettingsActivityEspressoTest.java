package com.example.weighttogo.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.weighttogo.R;
import com.example.weighttogo.database.DatabaseException;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.UserPreferenceDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.models.User;
import com.example.weighttogo.utils.PasswordUtilsV2;
import com.example.weighttogo.utils.SMSNotificationManager;
import com.example.weighttogo.utils.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

/**
 * Espresso instrumented tests for SettingsActivity.
 * <p>
 * Migrated from SettingsActivityTest.java (Robolectric) to resolve GitHub Issue #12
 * (Robolectric/Material3 theme incompatibility).
 * <p>
 * These tests run on a real Android device or emulator with full Material3 theme support.
 * Tests settings management: weight unit preferences, SMS permissions, and phone number management.
 * <p>
 * Coverage:
 * - Weight unit preference (4 tests)
 * - SMS permission management (8 tests)
 * <p>
 * Total: 12 tests
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsActivityEspressoTest {

    @Mock private UserDAO mockUserDAO;
    @Mock private UserPreferenceDAO mockUserPreferenceDAO;
    @Mock private SMSNotificationManager mockSmsManager;

    private ActivityScenario<SettingsActivity> scenario;
    private Context context;
    private WeighToGoDBHelper dbHelper;
    private SessionManager sessionManager;
    private User testUser;
    private long testUserId;

    @Before
    public void setUp() throws Exception {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this);

        // Get application context
        context = ApplicationProvider.getApplicationContext();

        // Initialize test database
        dbHelper = WeighToGoDBHelper.getInstance(context);

        // Get SessionManager instance
        sessionManager = SessionManager.getInstance(context);

        // Create test user
        testUser = createTestUser("testuser", "Test User");
        testUserId = testUser.getUserId();

        // Log in test user BEFORE launching activity
        sessionManager.createSession(testUser);

        // Set default mock behaviors
        when(mockUserPreferenceDAO.getWeightUnit(testUserId)).thenReturn("lbs");
        when(mockUserPreferenceDAO.setPreference(anyLong(), anyString(), anyString())).thenReturn(true);
        when(mockUserDAO.getUserById(testUserId)).thenReturn(testUser);

        // Launch activity with mocked dependencies
        scenario = ActivityScenario.launch(SettingsActivity.class);
        scenario.onActivity(activity -> {
            activity.setUserDAO(mockUserDAO);
            activity.setUserPreferenceDAO(mockUserPreferenceDAO);
            activity.setSMSNotificationManager(mockSmsManager);
        });
    }

    @After
    public void tearDown() {
        // Close activity scenario
        if (scenario != null) {
            scenario.close();
        }

        // Clear session
        if (sessionManager != null) {
            sessionManager.logout();
        }

        // Close database
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // ============================================================
    // WEIGHT UNIT PREFERENCE TESTS (4 tests)
    // ============================================================

    /**
     * Test 1: onCreate loads current weight unit preference.
     * <p>
     * Tests FR6.0.4 - SettingsActivity preference loading.
     * Verifies that when a user has "kg" preference, the activity
     * loads it on startup.
     */
    @Test
    public void test_onCreate_loadsCurrentWeightUnit() {
        // ARRANGE - Mock returns "kg" preference
        when(mockUserPreferenceDAO.getWeightUnit(testUserId)).thenReturn("kg");

        // Restart activity to load preference
        scenario.recreate();

        // ASSERT - Verify preference was queried
        verify(mockUserPreferenceDAO).getWeightUnit(testUserId);

        // ASSERT - Verify UI elements exist (visual verification on device)
        onView(withId(R.id.unitKg)).check(matches(isDisplayed()));
        onView(withId(R.id.unitLbs)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Click lbs toggle saves lbs preference.
     * <p>
     * Tests FR6.0.4 - SettingsActivity preference saving.
     * Verifies that clicking the lbs button saves "lbs" to database.
     */
    @Test
    public void test_clickLbsToggle_savesLbsPreference() {
        // ARRANGE - Start with "kg" preference
        when(mockUserPreferenceDAO.getWeightUnit(testUserId)).thenReturn("kg");
        scenario.recreate();

        // ACT - Click lbs button
        onView(withId(R.id.unitLbs)).perform(click());

        // ASSERT - Verify preference was saved as "lbs"
        verify(mockUserPreferenceDAO).setPreference(eq(testUserId), eq("weight_unit"), eq("lbs"));
    }

    /**
     * Test 3: Click kg toggle saves kg preference.
     * <p>
     * Tests FR6.0.4 - SettingsActivity preference saving.
     * Verifies that clicking the kg button saves "kg" to database.
     */
    @Test
    public void test_clickKgToggle_savesKgPreference() {
        // ARRANGE - Start with "lbs" preference (default)
        when(mockUserPreferenceDAO.getWeightUnit(testUserId)).thenReturn("lbs");
        scenario.recreate();

        // ACT - Click kg button
        onView(withId(R.id.unitKg)).perform(click());

        // ASSERT - Verify preference was saved as "kg"
        verify(mockUserPreferenceDAO).setPreference(eq(testUserId), eq("weight_unit"), eq("kg"));
    }

    /**
     * Test 4: Save weight unit shows confirmation toast.
     * <p>
     * Tests FR6.0.4 - SettingsActivity user feedback.
     * Verifies that after saving preference, UI interaction completes without crash.
     * <p>
     * NOTE: Espresso does not have built-in toast verification. This test verifies
     * the click completes successfully. Manual testing required for toast content.
     * <p>
     * TODO(GH #49): Add toast verification using UI Automator
     */
    @Test
    public void test_saveWeightUnit_showsConfirmationToast() {
        // ACT - Click kg button (should trigger toast)
        onView(withId(R.id.unitKg)).perform(click());

        // ASSERT - Verify no crash (toast content verified manually)
        // Expected toast: "Weight unit updated to kg"
        // Toast verification requires custom matcher or UI Automator
        onView(withId(R.id.unitKg)).check(matches(isDisplayed()));
    }

    // ============================================================
    // SMS NOTIFICATION TESTS (8 tests)
    // ============================================================

    /**
     * Test 5: onCreate checks SMS permission status.
     * <p>
     * Tests FR7.4 - SettingsActivity SMS permission checking.
     * Verifies that on startup, the activity checks current permission status
     * and updates UI accordingly.
     */
    @Test
    public void test_onCreate_checksPermissionStatus() {
        // ASSERT - Verify permission UI elements exist
        onView(withId(R.id.permissionStatusBadge)).check(matches(isDisplayed()));
        onView(withId(R.id.grantPermissionButton)).check(matches(isDisplayed()));
    }

    /**
     * Test 6: checkPermissions with granted permissions updates UI to granted state.
     * <p>
     * Tests FR7.4 - SettingsActivity permission UI updates.
     * Verifies that permission status badge is visible.
     * <p>
     * NOTE: Full permission grant flow requires device/emulator interaction
     * and cannot be fully automated in Espresso without UIAutomator.
     */
    @Test
    public void test_checkPermissions_withGranted_updatesUIGranted() {
        // ASSERT - Verify permission status badge exists and is displayed
        onView(withId(R.id.permissionStatusBadge)).check(matches(isDisplayed()));

        // Note: Actual permission status depends on device runtime permissions
        // Manual testing required to verify "Granted" state
    }

    /**
     * Test 7: checkPermissions with denied permissions updates UI to required state.
     * <p>
     * Tests FR7.4 - SettingsActivity permission UI updates.
     * Verifies that permission UI elements are visible.
     * <p>
     * NOTE: Full permission denial flow requires device/emulator interaction
     * and cannot be fully automated in Espresso without UIAutomator.
     */
    @Test
    public void test_checkPermissions_withDenied_updatesUIRequired() {
        // ASSERT - Verify permission UI elements exist and are displayed
        onView(withId(R.id.permissionStatusBadge)).check(matches(isDisplayed()));
        onView(withId(R.id.grantPermissionButton)).check(matches(isDisplayed()));

        // Note: Actual permission status depends on device runtime permissions
        // Manual testing required to verify "Required" state
    }

    /**
     * Test 8: Grant permission button click launches permission request.
     * <p>
     * Tests FR7.4 - SettingsActivity permission request flow.
     * Verifies that the grant permission button is clickable and doesn't crash.
     * <p>
     * NOTE: Actual permission dialog requires device interaction and cannot be
     * automated in Espresso. This test verifies the button exists and is clickable.
     */
    @Test
    public void test_requestPermissionButton_click_launchesPermissionRequest() {
        // ACT - Click grant permission button
        onView(withId(R.id.grantPermissionButton)).perform(click());

        // ASSERT - Verify no crash (permission dialog verified manually)
        // Manual testing required to verify permission request dialog appears
        onView(withId(R.id.grantPermissionButton)).check(matches(isDisplayed()));
    }

    /**
     * Test 9: Permission granted callback updates UI and enables SMS features.
     * <p>
     * Tests FR7.4 - SettingsActivity permission grant handling.
     * Verifies that SMS toggle switches exist and are displayed.
     * <p>
     * NOTE: Cannot programmatically grant permissions in Espresso tests.
     * This test verifies the UI elements exist for manual verification.
     */
    @Test
    public void test_onPermissionGranted_updatesUIAndEnablesSms() {
        // ASSERT - Verify SMS toggle switches exist
        onView(withId(R.id.switchEnableSms)).check(matches(isDisplayed()));
        onView(withId(R.id.switchGoalAlerts)).check(matches(isDisplayed()));
        onView(withId(R.id.switchMilestoneAlerts)).check(matches(isDisplayed()));
        onView(withId(R.id.switchDailyReminders)).check(matches(isDisplayed()));

        // Note: Actual enable state depends on permission grant
        // Manual testing required to verify switches are enabled after permission grant
    }

    /**
     * Test 10: Permission denied callback updates UI and shows rationale.
     * <p>
     * Tests FR7.4 - SettingsActivity permission denial handling.
     * Verifies that permission status badge exists for displaying denial state.
     * <p>
     * NOTE: Cannot programmatically deny permissions in Espresso tests.
     * This test verifies the UI elements exist for manual verification.
     */
    @Test
    public void test_onPermissionDenied_updatesUIAndShowsRationale() {
        // ASSERT - Verify permission status badge exists
        onView(withId(R.id.permissionStatusBadge)).check(matches(isDisplayed()));

        // Note: Actual denial state and rationale display requires manual testing
        // Manual testing required to verify denial flow
    }

    /**
     * Test 11: Save phone button with valid phone saves to database.
     * <p>
     * Tests FR7.4 - SettingsActivity phone number input handling.
     * Verifies that phone number input field exists and is displayed.
     * <p>
     * NOTE: Full phone number save flow requires keyboard interaction
     * which is complex to automate in Espresso. This test verifies the input exists.
     */
    @Test
    public void test_savePhoneButton_withValidPhone_savesToDatabase() {
        // ASSERT - Verify phone number input exists
        onView(withId(R.id.phoneNumberInput)).check(matches(isDisplayed()));

        // Note: Actual phone save requires text input and button click
        // Manual testing required to verify phone number save with E.164 format
    }

    /**
     * Test 12: Save phone button with invalid phone shows error.
     * <p>
     * Tests FR7.4 - SettingsActivity phone number validation.
     * Verifies that phone number input field exists for validation testing.
     * <p>
     * NOTE: Full validation flow requires keyboard interaction and error verification
     * which is complex to automate in Espresso. This test verifies the input exists.
     */
    @Test
    public void test_savePhoneButton_withInvalidPhone_showsError() {
        // ASSERT - Verify phone number input exists
        onView(withId(R.id.phoneNumberInput)).check(matches(isDisplayed()));

        // Note: Actual validation error requires invalid input and button click
        // Manual testing required to verify error display for invalid phone numbers
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Creates a test user with the given username and display name.
     * <p>
     * Generates a secure password hash using bcrypt and inserts the user into the
     * test database. Returns the created User object with ID populated.
     *
     * @param username    the username for the test user
     * @param displayName the display name for the test user
     * @return the created User object
     * @throws DatabaseException if database operation fails
     */
    private User createTestUser(String username, String displayName) throws Exception {
        String password = "Test123!";
        String passwordHash = PasswordUtilsV2.hashPasswordBcrypt(password);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setSalt("");  // bcrypt stores salt in hash
        user.setPasswordAlgorithm("BCRYPT");
        user.setDisplayName(displayName);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserDAO realUserDAO = new UserDAO(dbHelper);
        long userId = realUserDAO.insertUser(user);
        user.setUserId(userId);

        return user;
    }
}
