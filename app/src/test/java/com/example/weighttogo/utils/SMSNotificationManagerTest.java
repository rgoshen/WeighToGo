package com.example.weighttogo.utils;

import android.content.Context;
import android.telephony.SmsManager;

import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.UserPreferenceDAO;
import com.example.weighttogo.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SMSNotificationManager following strict TDD.
 * Tests SMS sending, permission checking, and preference handling.
 *
 * Uses Mockito to mock SmsManager since we cannot actually send SMS during tests.
 *
 * TDD Approach: Write ONE failing test at a time, implement minimal code to pass,
 * then move to next test.
 */
@RunWith(RobolectricTestRunner.class)
public class SMSNotificationManagerTest {

    private Context context;
    private SMSNotificationManager smsManager;

    @Mock
    private SmsManager mockSmsManager;

    @Mock
    private UserDAO mockUserDAO;

    @Mock
    private UserPreferenceDAO mockUserPreferenceDAO;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        // SMSNotificationManager will be initialized in individual tests
    }

    // =============================================================================================
    // PERMISSION CHECKING TESTS (3 tests) - Phase 7.3 Commit 9
    // =============================================================================================

    /**
     * Test 1: hasSmsSendPermission() returns true when SEND_SMS permission granted
     */
    @Test
    public void test_hasSmsSendPermission_withGranted_returnsTrue() {
        fail("Test not implemented yet - RED phase");
    }

    /**
     * Test 2: hasSmsSendPermission() returns false when SEND_SMS permission denied
     */
    @Test
    public void test_hasSmsSendPermission_withDenied_returnsFalse() {
        fail("Test not implemented yet - RED phase");
    }

    /**
     * Test 3: hasPostNotificationsPermission() checks POST_NOTIFICATIONS on Android 13+
     */
    @Test
    public void test_hasPostNotificationsPermission_android13Plus_checksPermission() {
        fail("Test not implemented yet - RED phase");
    }

    // =============================================================================================
    // PREFERENCE CHECKING TESTS (4 tests) - Phase 7.3 Commit 9
    // =============================================================================================

    /**
     * Test 4: canSendSms() returns true when all conditions met
     */
    @Test
    public void test_canSendSms_allConditionsMet_returnsTrue() {
        fail("Test not implemented yet - RED phase");
    }

    /**
     * Test 5: canSendSms() returns false when user has no phone number
     */
    @Test
    public void test_canSendSms_noPhoneNumber_returnsFalse() {
        fail("Test not implemented yet - RED phase");
    }

    /**
     * Test 6: canSendSms() returns false when SMS notifications disabled
     */
    @Test
    public void test_canSendSms_smsDisabled_returnsFalse() {
        fail("Test not implemented yet - RED phase");
    }

    /**
     * Test 7: canSendSms() returns false when permissions not granted
     */
    @Test
    public void test_canSendSms_noPermission_returnsFalse() {
        fail("Test not implemented yet - RED phase");
    }

    // =============================================================================================
    // MESSAGE SENDING TESTS (5 tests) - Phase 7.3 Commit 9
    // =============================================================================================

    /**
     * Test 8: sendGoalAchievedSms() sends message when conditions met
     *
     * This test will fail until Commit 13 implements actual SMS sending.
     */
    @Test
    public void test_sendGoalAchievedSms_withValidConditions_sendsMessage() {
        // ARRANGE
        long userId = 1L;
        double goalWeight = 150.0;
        String unit = "lbs";
        String phone = "+12025551234";

        // Mock user with phone number
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPhoneNumber(phone);
        when(mockUserDAO.getUserById(userId)).thenReturn(mockUser);

        // Mock preferences - all enabled
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_SMS_ENABLED, "false"))
                .thenReturn("true");
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_GOAL_ALERTS, "true"))
                .thenReturn("true");

        smsManager = SMSNotificationManager.getInstance(context, mockUserDAO, mockUserPreferenceDAO);

        // ACT
        boolean result = smsManager.sendGoalAchievedSms(userId, goalWeight, unit);

        // ASSERT
        assertTrue("Should return true when SMS sent successfully", result);
        // Note: Verification of SmsManager.sendTextMessage() will be added in Commit 13
    }

    /**
     * Test 9: sendGoalAchievedSms() does not send when goal alerts disabled
     */
    @Test
    public void test_sendGoalAchievedSms_goalAlertsDisabled_doesNotSend() {
        // ARRANGE
        long userId = 1L;
        double goalWeight = 150.0;
        String unit = "lbs";
        String phone = "+12025551234";

        // Mock user with phone number
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPhoneNumber(phone);
        when(mockUserDAO.getUserById(userId)).thenReturn(mockUser);

        // Mock preferences - goal alerts DISABLED
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_SMS_ENABLED, "false"))
                .thenReturn("true");
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_GOAL_ALERTS, "true"))
                .thenReturn("false");  // Goal alerts disabled

        smsManager = SMSNotificationManager.getInstance(context, mockUserDAO, mockUserPreferenceDAO);

        // ACT
        boolean result = smsManager.sendGoalAchievedSms(userId, goalWeight, unit);

        // ASSERT
        assertFalse("Should return false when goal alerts disabled", result);
    }

    /**
     * Test 10: sendMilestoneSms() sends message when conditions met
     *
     * This test will fail until Commit 13 implements actual SMS sending.
     */
    @Test
    public void test_sendMilestoneSms_withValidConditions_sendsMessage() {
        // ARRANGE
        long userId = 1L;
        int milestone = 10;
        String unit = "lbs";
        String phone = "+12025551234";

        // Mock user with phone number
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPhoneNumber(phone);
        when(mockUserDAO.getUserById(userId)).thenReturn(mockUser);

        // Mock preferences - all enabled
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_SMS_ENABLED, "false"))
                .thenReturn("true");
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_MILESTONE_ALERTS, "true"))
                .thenReturn("true");

        smsManager = SMSNotificationManager.getInstance(context, mockUserDAO, mockUserPreferenceDAO);

        // ACT
        boolean result = smsManager.sendMilestoneSms(userId, milestone, unit);

        // ASSERT
        assertTrue("Should return true when SMS sent successfully", result);
    }

    /**
     * Test 11: sendMilestoneSms() does not send when milestone alerts disabled
     */
    @Test
    public void test_sendMilestoneSms_milestoneAlertsDisabled_doesNotSend() {
        // ARRANGE
        long userId = 1L;
        int milestone = 5;
        String unit = "lbs";
        String phone = "+12025551234";

        // Mock user with phone number
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPhoneNumber(phone);
        when(mockUserDAO.getUserById(userId)).thenReturn(mockUser);

        // Mock preferences - milestone alerts DISABLED
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_SMS_ENABLED, "false"))
                .thenReturn("true");
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_MILESTONE_ALERTS, "true"))
                .thenReturn("false");  // Milestone alerts disabled

        smsManager = SMSNotificationManager.getInstance(context, mockUserDAO, mockUserPreferenceDAO);

        // ACT
        boolean result = smsManager.sendMilestoneSms(userId, milestone, unit);

        // ASSERT
        assertFalse("Should return false when milestone alerts disabled", result);
    }

    /**
     * Test 12: sendDailyReminderSms() sends message when conditions met
     *
     * This test will fail until Commit 13 implements actual SMS sending.
     */
    @Test
    public void test_sendDailyReminderSms_withValidConditions_sendsMessage() {
        // ARRANGE
        long userId = 1L;
        String phone = "+12025551234";

        // Mock user with phone number
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setPhoneNumber(phone);
        when(mockUserDAO.getUserById(userId)).thenReturn(mockUser);

        // Mock preferences - all enabled
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_SMS_ENABLED, "false"))
                .thenReturn("true");
        when(mockUserPreferenceDAO.getPreference(userId, SMSNotificationManager.KEY_REMINDER_ENABLED, "false"))
                .thenReturn("true");

        smsManager = SMSNotificationManager.getInstance(context, mockUserDAO, mockUserPreferenceDAO);

        // ACT
        boolean result = smsManager.sendDailyReminderSms(userId);

        // ASSERT
        assertTrue("Should return true when SMS sent successfully", result);
    }
}
